package com.logistics.user.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 실제 JWT 인증은 이후 로그인 및 Gateway 인증 이슈에서 추가
 */
@Configuration
public class SecurityConfig {

    /**
     * 비밀번호 단방향 암호화
     * PasswordEncoder 인터페이스에 의존하도록 Bean으로 등록
     */
    @Bean	
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 아직 JWT 인증 필터가 구현되지 않았음.
     * 이후 Gateway 및 JWT 인증이 적용되면 정책을 변경한다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // REST API이므로 브라우저 세션 기반 CSRF는 비활성화
                .csrf(csrf -> csrf.disable())

                // 현재 이슈에서는 인증 기능을 구현하지 않으므로 전체 허용
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}