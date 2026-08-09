package com.logistics.hub.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "hub.exchange";
    public static final String QUEUE_NAME = "hub.deleted.queue";
    public static final String ROUTING_KEY = "hub.deleted";

    @Bean
    public TopicExchange hubExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue hubDeletedQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue hubDeletedQueue, TopicExchange hubExchange) {
        return BindingBuilder.bind(hubDeletedQueue).to(hubExchange).with(ROUTING_KEY);
    }

    // 4.0 이상 호환 Bean
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}