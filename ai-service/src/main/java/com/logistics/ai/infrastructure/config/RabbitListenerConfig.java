package com.logistics.ai.infrastructure.config;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.logistics.ai.infrastructure.feign.exception.RetryRemoteException;

import feign.RetryableException;

@Configuration
public class RabbitListenerConfig {

	@Bean
	StatelessRetryOperationsInterceptor orderCreatedRetryInterceptor() {
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
				.recoverer(new RejectAndDontRequeueRecoverer())
				.build();
	}
	
	@Bean
	SimpleRabbitListenerContainerFactory orderCreatedRabbitListenerContainerFactory(
			ConnectionFactory connectionFactory,
			StatelessRetryOperationsInterceptor orderCreatedRetryInterceptor) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		
		factory.setConnectionFactory(connectionFactory);
		
		factory.setAdviceChain(orderCreatedRetryInterceptor);
		
		return factory;
	}
	
}
