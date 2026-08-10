package com.logistics.inventory.infrastructure.config;

import com.logistics.company.infrastructure.security.filter.HeaderAuthenticationFilter;
import com.logistics.inventory.domain.entity.Role;
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
				
				// PATCH /api/v1/companies/manager/fix
				.requestMatchers(HttpMethod.PATCH, "/api/v1/companies/update/manager/fix").hasAnyRole(
						Role.MASTER.name(), Role.HUB_MANAGER.name()
				)
				
				// DELETED /api/v1/companies/{companyId}
				.requestMatchers(HttpMethod.DELETE, "/api/v1/companies/{companyId}").hasAnyRole(
						Role.MASTER.name(), Role.HUB_MANAGER.name(), Role.COMPANY_MANAGER.name()
				)
				
				// PATCH /api/v1/companies/{companyId}
				.requestMatchers(HttpMethod.PATCH, "/api/v1/companies/{companyId}").hasAnyRole(
						Role.MASTER.name(), Role.HUB_MANAGER.name(), Role.COMPANY_MANAGER.name()
				)
				
				// POST /api/v1/companies
				.requestMatchers(HttpMethod.POST, "/api/v1/companies").hasAnyRole(
						Role.MASTER.name(), Role.HUB_MANAGER.name()
				)
				
				.requestMatchers(HttpMethod.GET,
						"/api/v1/companies/{companyId}",
						"/api/v1/companies"
				).permitAll()
				
				// 내부 API 권한 검증은 어떻게?
				.requestMatchers(
						"/internal/v1/companies",
						"/internal/v1/companies/{companyId}/exists"
				).permitAll()
				
				.anyRequest().authenticated()
		)
		.addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
		
		;
		return http.build();
	}
	
	
}
