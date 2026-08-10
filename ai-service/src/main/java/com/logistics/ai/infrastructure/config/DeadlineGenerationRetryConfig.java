package com.logistics.ai.infrastructure.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

import com.logistics.ai.infrastructure.feign.exception.RetryRemoteException;

import feign.RetryableException;

@Configuration
public class DeadlineGenerationRetryConfig {

	@Bean
	RetryTemplate deadlineGenerationRetryTemplate() {
		RetryPolicy retryPolicy = RetryPolicy.builder()
        .maxRetries(2)
        .delay(Duration.ofSeconds(1))
        .multiplier(2.0)
        .maxDelay(Duration.ofSeconds(10))
        .includes(
                RetryRemoteException.class,
                RetryableException.class
        )
        .build();
		return new RetryTemplate(retryPolicy);
	}
}
