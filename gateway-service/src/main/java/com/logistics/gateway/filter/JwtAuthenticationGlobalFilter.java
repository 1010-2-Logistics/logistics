package com.logistics.gateway.filter;

import com.logistics.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * X-User-Id / X-User-Role / X-Hub-Id / X-Company-Id 헤더의 유일한 발급처.
 *
 * 들어온 요청의 해당 헤더는 화이트리스트 판정보다 먼저 무조건 제거한다.
 * 이 순서를 지키지 않으면 Authorization 없이 헤더만 위조해도 통과한다(S-1).
 */
@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> FORGEABLE_HEADERS = List.of(
            "X-User-Id", "X-User-Role", "X-Hub-Id", "X-Company-Id"
    );

    private static final String ACCESS_TOKEN_TYPE = "ACCESS";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtAuthenticationGlobalFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest strippedRequest = exchange.getRequest().mutate()
                .headers(headers -> FORGEABLE_HEADERS.forEach(headers::remove))
                .build();
        ServerWebExchange strippedExchange = exchange.mutate().request(strippedRequest).build();

        String path = strippedRequest.getURI().getPath();
        if (jwtProperties.whitelist().stream().anyMatch(path::startsWith)) {
            return chain.filter(strippedExchange);
        }

        String authorization = strippedRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(strippedExchange);
        }

        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(authorization.substring("Bearer ".length()))
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(strippedExchange);
        }

        if (!ACCESS_TOKEN_TYPE.equals(claims.get("tokenType", String.class))) {
            return unauthorized(strippedExchange);
        }

        ServerHttpRequest authenticatedRequest = strippedRequest.mutate()
                .headers(headers -> {
                    headers.set("X-User-Id", claims.getSubject());
                    headers.set("X-User-Role", claims.get("role", String.class));
                    setIfPresent(headers, "X-Hub-Id", claims.get("hubId", String.class));
                    setIfPresent(headers, "X-Company-Id", claims.get("companyId", String.class));
                })
                .build();

        return chain.filter(strippedExchange.mutate().request(authenticatedRequest).build());
    }

    private void setIfPresent(HttpHeaders headers, String name, String value) {
        if (value != null) {
            headers.set(name, value);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
