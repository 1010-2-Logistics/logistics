package com.logistics.delivery.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.infrastructure.security.filter.HeaderAuthenticationFilter;

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
                        .requestMatchers(HttpMethod.GET, "/api/v1/deliveries", "/api/v1/deliveries/{deliveryId}",
                                "/api/v1/deliveries/{deliveryId}/routes").hasAnyRole(
                                Role.MASTER.name(), Role.HUB_MANAGER.name(), Role.HUB_DELIVERY_MANAGER.name(),
                                Role.COMPANY_DELIVERY_MANAGER.name(), Role.COMPANY_MANAGER.name())

                        .requestMatchers(HttpMethod.PATCH, "/api/v1/deliveries/{deliveryId}").hasAnyRole(
                                Role.MASTER.name(), Role.COMPANY_DELIVERY_MANAGER.name())

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/deliveries/{deliveryId}").hasAnyRole(
                                Role.MASTER.name(), Role.HUB_MANAGER.name())

                        .requestMatchers(HttpMethod.PATCH, "/api/v1/deliveries/{deliveryId}/routes/{routeId}").hasAnyRole(
                                Role.MASTER.name(), Role.HUB_MANAGER.name(), Role.HUB_DELIVERY_MANAGER.name(),
                                Role.COMPANY_DELIVERY_MANAGER.name())

                        .requestMatchers(HttpMethod.GET, "/api/v1/delivery-managers", "/api/v1/delivery-managers/{deliveryManagerId}")
                        .hasAnyRole(Role.MASTER.name(), Role.HUB_MANAGER.name(), Role.HUB_DELIVERY_MANAGER.name(),
                                Role.COMPANY_DELIVERY_MANAGER.name())

                        .requestMatchers(HttpMethod.POST, "/api/v1/delivery-managers").hasAnyRole(
                                Role.MASTER.name(), Role.HUB_MANAGER.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/delivery-managers/{deliveryManagerId}").hasAnyRole(
                                Role.MASTER.name(), Role.HUB_MANAGER.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/delivery-managers/{deliveryManagerId}").hasAnyRole(
                                Role.MASTER.name(), Role.HUB_MANAGER.name())

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        .requestMatchers("/internal/v1/deliveries/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
