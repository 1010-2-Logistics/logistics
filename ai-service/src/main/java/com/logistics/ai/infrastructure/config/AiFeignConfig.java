package com.logistics.ai.infrastructure.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;

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
	
	@Bean
  Request.Options feignRequestOptions() {
      return new Request.Options(3,
      		TimeUnit.SECONDS, 30,
      		TimeUnit.SECONDS,
      		true
      );
  }
	
	
}
