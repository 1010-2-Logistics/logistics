package com.logistics.user.global.config;

import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.infrastructure.security.MockGatewayAuthenticationFilter;
import org.springframework.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 실제 JWT 인증은 이후 로그인 및 Gateway 인증 이슈에서 추가
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final MockGatewayAuthenticationFilter
            mockGatewayAuthenticationFilter;

    /**
     * 비밀번호 단방향 암호화
     * PasswordEncoder 인터페이스에 의존하도록 Bean으로 등록
     */
    @Bean	
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                /*
                 * JWT 기반 시스템은 서버 세션에 인증 정보를 저장하지 않는다.
                 * 매 요청마다 Gateway 헤더를 통해 인증 정보를 만든다.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",

                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 내 정보 관련 API
                        // 로그인 사용자라면 모두 접근 가능
                        .requestMatchers(
                                "/api/v1/users/me",
                                "/api/v1/users/me/**"
                        ).authenticated()

                        // 사용자 목록 / 상세 조회
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/users",
                                "/api/v1/users/{userId}"
                        ).hasAnyRole(
                                UserRole.MASTER.name(),
                                UserRole.HUB_MANAGER.name()
                        )

                        // 가입 승인 / 거절
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/users/{userId}/approval"
                        ).hasAnyRole(
                                UserRole.MASTER.name(),
                                UserRole.HUB_MANAGER.name()
                        )

                        // 관리자 사용자 삭제
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/users/{userId}"
                        ).hasRole(
                                UserRole.MASTER.name()
                        )

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                /*
                 * Controller 실행 전에 목업 헤더를 읽어
                 * Authentication을 생성한다.
                 */
                .addFilterBefore(
                        mockGatewayAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}