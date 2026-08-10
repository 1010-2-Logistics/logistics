package com.logistics.ai.infrastructure.config;

import java.time.Duration;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

import com.logistics.ai.infrastructure.exception.DeadlineGenerationRetryException;
import com.logistics.ai.infrastructure.exception.RetryRemoteException;

import feign.RetryableException;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitListenerConfig {
	
	@Bean
	JacksonJsonMessageConverter jacksonJsonMessageConverter(JsonMapper jsonMapper) {
		return new JacksonJsonMessageConverter(jsonMapper);
	}
	
	@Bean("orderCreatedRetryTemplate")
	RetryTemplate orderCreatedRetryTemplate() {
		RetryPolicy retryPolicy = RetryPolicy.builder()
				.maxRetries(2)
				.delay(Duration.ofSeconds(2))
				.multiplier(2.0)
				.maxDelay(Duration.ofSeconds(60))
				.predicate(RabbitListenerConfig::isOrderCreatedRetryable)
				.build();
		
		return new RetryTemplate(retryPolicy);
	}
	
	@Bean
	SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			ConnectionFactory connectionFactory,
			JacksonJsonMessageConverter rabbitMessageConvertor
	) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(rabbitMessageConvertor);
		
		return factory;
	}
	
	private static boolean isOrderCreatedRetryable(Throwable throwable) {
		if(hasCause(throwable, DeadlineGenerationRetryException.class)) {
			return false;
		}
		
		return hasCause(throwable, RetryRemoteException.class) || hasCause(throwable, RetryableException.class);
	}

	private static boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
		Throwable current = throwable;
		
		while(current != null) {
			if(type.isInstance(current)) {
				return true;
			}
			
			if(current == current.getCause()) {
				break;
			}
			
			current = current.getCause();
		}
		return false;
	}
	
	
}
