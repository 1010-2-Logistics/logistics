package com.logistics.ai.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	@Value("${rabbitmq.order-created.exchange}")
	private String orderCreatedExchange;
  
  @Value("${rabbitmq.order-created.queue}")
  private String orderCreatedQueue;
  
  @Bean
  TopicExchange orderCreatedExchange() {
  	return new TopicExchange(orderCreatedExchange);
  }
  
  @Bean
  Queue orderCreatedQueue() {
  	return new Queue(orderCreatedQueue);
  }
  
  @Bean
  Binding orderCreatedBinding() {
  	return BindingBuilder
  			.bind(orderCreatedQueue())
  			.to(orderCreatedExchange())
  			.with(orderCreatedQueue);		
  }
  
}
