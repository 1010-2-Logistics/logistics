package com.logistics.user.global.config;

import com.logistics.user.infrastructure.security.MockGatewayAuthenticationFilter;
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
                        /*
                         * 회원가입과 로그인은 인증 없이 접근 가능.
                         */
                        .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login"
                        ).permitAll()

                        /*
                         * 그 외 요청은 Authentication이 필요하다.
                         */
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