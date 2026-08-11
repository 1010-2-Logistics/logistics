package com.logistics.ai.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class AiFeignConfig {

	@Value("${ai.gemini.key}")
	private String apiKey;
	
	@Bean
  RequestInterceptor requestInterceptor() {
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate template) {
				template.header("x-goog-api-key", apiKey);
				template.header("Content-Type", "application/json");
			}
		};
  }
	
	
}
