package com.logistics.ai.infrastructure.config;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.logistics.ai.infrastructure.feign.exception.RetryRemoteException;
import com.logistics.ai.infrastructure.messaging.OrderCreatedMessageRecover;

import feign.RetryableException;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitListenerConfig {
	
	@Bean
	JacksonJsonMessageConverter rabbitMessageConvertor(JsonMapper jsonMapper) {
		return new JacksonJsonMessageConverter(jsonMapper);
	}
	
	@Bean
	StatelessRetryOperationsInterceptor orderCreatedRetryInterceptor(OrderCreatedMessageRecover orderCreatedMessageRecover) {
		return RetryInterceptorBuilder
				.stateless()
				.configureRetryPolicy(policy -> policy
						.maxRetries(2) // 최초 1회 + 재시도 2회 = 총 3회
						.includes(RetryRemoteException.class, RetryableException.class) // Retry 대상을 RetryRemoteException 계열로 한정
				)
				.backOffOptions(
						1000,
						2.0,
						10000
				)
				.recoverer(orderCreatedMessageRecover)
				.build();
	}
	
	@Bean
	SimpleRabbitListenerContainerFactory orderCreatedRabbitListenerContainerFactory(
			ConnectionFactory connectionFactory,
			StatelessRetryOperationsInterceptor orderCreatedRetryInterceptor,
			JacksonJsonMessageConverter rabbitMessageConvertor) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		
		factory.setConnectionFactory(connectionFactory);
		
		factory.setAdviceChain(orderCreatedRetryInterceptor);
		
		factory.setMessageConverter(rabbitMessageConvertor);
		
		return factory;
	}
	
}
