package com.logistics.user.infrastructure.security;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml의 jwt 설정값을 객체로 묶는다.
 *
 * 역할:
 * - JWT Secret 제공
 * - Access Token 만료 시간 제공
 * - Refresh Token 만료 시간 제공
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        /**
         * JWT 서명 및 검증에 사용하는 비밀키.
         */
        String secret,

        /**
         * Access Token 유효기간.
         * 단위는 초이다.
         */
        long accessTokenExpirationSeconds,

        /**
         * Refresh Token 유효기간.
         * 단위는 초이다.
         */
        long refreshTokenExpirationSeconds

) {
}