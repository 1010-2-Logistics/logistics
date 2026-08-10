package com.logistics.order.infrastructure.config;

import com.logistics.order.domain.entity.Role;
import com.logistics.order.infrastructure.security.filter.HeaderAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 주문 생성
                        // 모든 로그인 사용자
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/orders"
                        ).authenticated()

                        // 주문 수정
                        // MASTER, 담당 HUB_MANAGER
                        // 담당 허브 여부는 서비스 내부에서 추가 검증
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/orders/{orderId}"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name()
                        )

                        // 주문 취소
                        // 주문 상태를 변경하는 작업이므로
                        // 우선 수정 권한과 동일하게 적용
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/orders/{orderId}/cancel"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name()
                        )

                        // 주문 삭제
                        // MASTER, 담당 HUB_MANAGER
                        // 담당 허브 여부는 서비스 내부에서 추가 검증
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/orders/{orderId}"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name()
                        )

                        // 주문 단건 조회 / 검색
                        // 모든 로그인 사용자
                        // 본인 주문, 담당 허브 등의 데이터 범위는 서비스에서 검증
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/orders/{orderId}",
                                "/api/v1/orders"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new HeaderAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
