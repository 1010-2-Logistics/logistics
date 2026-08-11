package com.logistics.ai.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.logistics.ai.infrastructure.security.filter.HeaderAuthenticationFilter;

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
