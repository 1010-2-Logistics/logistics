package com.logistics.inventory.infrastructure.config;

import com.logistics.inventory.domain.entity.Role;
import com.logistics.inventory.infrastructure.security.filter.HeaderAuthenticationFilter;
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
    SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 재고 생성
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/inventories"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name()
                        )

                        // 재고 수정
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/inventories/{inventoryId}"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name()
                        )

                        // 재고 삭제
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/inventories/{inventoryId}"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name()
                        )

                        // 재고 단건/목록 조회
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/inventories/**"
                        ).authenticated()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/internal/v1/inventories/deductions",
                                "/internal/v1/inventories/restorations"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new HeaderAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
