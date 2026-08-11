package com.logistics.hubRoute.infrastructure.config;


import com.logistics.hubRoute.domain.entity.Role;
import com.logistics.hubRoute.infrastructure.security.filter.HeaderAuthenticationFilter;
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

                        // PUT /api/v1/hubRoute/{hubRouteId}
                        .requestMatchers(HttpMethod.PUT, "/api/v1/hubRoute/{hubRouteId}").hasAnyRole(
                                Role.MASTER.name()
                        )

                        // DELETED /api/v1/hubRoute/{hubRouteId}
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/hubRoute/{hubRouteId}").hasAnyRole(
                                Role.MASTER.name()
                        )

                        // POST /api/v1/hubRoute
                        .requestMatchers(HttpMethod.POST, "/api/v1/hubRoute").hasAnyRole(
                                Role.MASTER.name()
                        )

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/hubRoute/**"
                        ).permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
