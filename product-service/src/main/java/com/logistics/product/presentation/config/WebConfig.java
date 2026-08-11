package com.logistics.product.presentation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.logistics.product.presentation.interceptor.AuthenticationInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final AuthenticationInterceptor authenticationInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authenticationInterceptor)
				.addPathPatterns("/api/**")
				.excludePathPatterns(
						"/internal/**",
						"/swagger-ui/**",
						"/v3/api-docs/**",
						"/actuator/health"
				);
	}
	
	
}
