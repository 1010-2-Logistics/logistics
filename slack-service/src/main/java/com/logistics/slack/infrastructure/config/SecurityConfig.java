package com.logistics.slack.infrastructure.config;

import com.logistics.slack.domain.entity.Role;
import com.logistics.slack.infrastructure.security.filter.HeaderAuthenticationFilter;
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

                        // Slack 메시지 발송
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/slack/messages"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name(),
                                Role.COMPANY_MANAGER.name(),
                                Role.HUB_DELIVERY_MANAGER.name(),
                                Role.COMPANY_DELIVERY_MANAGER.name()
                        )

                        // Slack 메시지 재발송
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/slack/messages/{slackMessageId}/retry"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name(),
                                Role.COMPANY_MANAGER.name(),
                                Role.HUB_DELIVERY_MANAGER.name(),
                                Role.COMPANY_DELIVERY_MANAGER.name()
                        )

                        // Slack 메시지 삭제
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/slack/messages/{slackMessageId}"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name(),
                                Role.COMPANY_MANAGER.name(),
                                Role.HUB_DELIVERY_MANAGER.name(),
                                Role.COMPANY_DELIVERY_MANAGER.name()
                        )

                        // Slack 메시지 단건/목록 조회
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/slack/messages/**"
                        ).hasAnyRole(
                                Role.MASTER.name(),
                                Role.HUB_MANAGER.name(),
                                Role.COMPANY_MANAGER.name(),
                                Role.HUB_DELIVERY_MANAGER.name(),
                                Role.COMPANY_DELIVERY_MANAGER.name()
                        )

                        // Swagger / health
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health"
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
