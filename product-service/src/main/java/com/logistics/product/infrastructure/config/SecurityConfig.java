package com.logistics.product.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.logistics.product.domain.entity.Role;
import com.logistics.product.infrastructure.security.filter.HeaderAuthenticationFilter;

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
				
				.requestMatchers(HttpMethod.DELETE, "/api/v1/products/{productId}").hasAnyRole(
						Role.MASTER.name(), Role.HUB_MANAGER.name()
				)
				
				.requestMatchers(HttpMethod.POST, "/api/v1/products").hasAnyRole(
						Role.MASTER.name(), Role.HUB_MANAGER.name(), Role.COMPANY_MANAGER.name()
				)
				
				.requestMatchers(HttpMethod.PATCH, "/api/v1/products").hasAnyRole(
						Role.MASTER.name(), Role.HUB_MANAGER.name(), Role.COMPANY_MANAGER.name()
				)
				
				.requestMatchers(HttpMethod.GET,
						"/api/v1/products/{companyId}",
						"/api/v1/products"
				).permitAll()
				
				.requestMatchers(HttpMethod.GET,
						"/internal/v1/products/{productId}",
						"/internal/v1/products/{productId}/exists"
				).permitAll()
				
				.requestMatchers(HttpMethod.PATCH,
						"/internal/v1/products"
				).permitAll()
				
				.requestMatchers(
						"/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health"
				).permitAll()
				
				
				.anyRequest().authenticated()
		)
		.addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
		
		;
		return http.build();
	}
}
