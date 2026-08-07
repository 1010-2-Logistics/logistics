package com.logistics.user.infrastructure.security;

import com.logistics.user.domain.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 생성
 *
 * API Gateway에서는 JWT 서명 및 만료 검증
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtProvider {

    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtProvider(
            JwtProperties jwtProperties
    ) {
        this.jwtProperties = jwtProperties;

        /*
         * 문자열 Secret을 HMAC 서명에 사용할 SecretKey로 변환한다.
         */
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 인증된 요청에 사용할 Access Token을 생성
     * Gateway는 이 Claim을 읽어 내부 사용자 헤더를 만든다.
     */
    public String createAccessToken(
            User user
    ) {
        Instant issuedAt = Instant.now();

        Instant expiresAt = issuedAt.plusSeconds(
                jwtProperties.accessTokenExpirationSeconds()
        );

        return Jwts.builder()
                // JWT 표준 subject에 사용자 ID 저장
                .subject(String.valueOf(user.getUserId()))

                // Gateway와 각 서비스에서 사용할 인증 정보
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .claim(
                        "hubId",
                        user.getHubId() == null
                                ? null
                                : user.getHubId().toString()
                )
                .claim(
                        "companyId",
                        user.getCompanyId() == null
                                ? null
                                : user.getCompanyId().toString()
                )

                // Access Token과 Refresh Token 구분
                .claim("tokenType", ACCESS_TOKEN_TYPE)

                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Access Token 재발급에 사용할 Refresh Token을 생성
     */
    public String createRefreshToken(
            User user
    ) {
        Instant issuedAt = Instant.now();

        Instant expiresAt = issuedAt.plusSeconds(
                jwtProperties.refreshTokenExpirationSeconds()
        );

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("tokenType", REFRESH_TOKEN_TYPE)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties
                .accessTokenExpirationSeconds();
    }

    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties
                .refreshTokenExpirationSeconds();
    }
}