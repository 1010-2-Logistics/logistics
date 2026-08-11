package com.logistics.ai.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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
  
  @Value("${rabbitmq.order-created.dlx}")
  private String orderCreatedDlx;
  
  @Value("${rabbitmq.order-created.dlq}")
  private String orderCreatedDlq;
  
  @Value("${rabbitmq.order-created.dlq-routing-key}")
  private String orderCreatedDlqRoutingKey;
  
  @Bean
  DirectExchange orderCreatedExchange() {
  	return new DirectExchange(orderCreatedExchange);
  }
  
  @Bean
  Queue orderCreatedQueue() {
  	return QueueBuilder
  			.durable(orderCreatedQueue)
  			.deadLetterExchange(orderCreatedDlx)
  			.deadLetterRoutingKey(orderCreatedDlqRoutingKey)
  			.build();
  }
  
  @Bean
  Binding orderCreatedBinding() {
  	return BindingBuilder
  			.bind(orderCreatedQueue())
  			.to(orderCreatedExchange())
  			.with(orderCreatedQueue);		
  }
  
  @Bean
  TopicExchange orderCreatedDlx() {
  	return new TopicExchange(orderCreatedDlx);
  }
  
  @Bean
  Queue orderCreatedDlq() {
  	return QueueBuilder
  			.durable(orderCreatedDlq)
  			.build();
  }
  
  @Bean
  Binding orderCreatedDlqBinding() {
  	return BindingBuilder
  			.bind(orderCreatedDlq())
  			.to(orderCreatedDlx())
  			.with(orderCreatedDlqRoutingKey);
  }
  
}
