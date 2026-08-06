package com.logistics.slack.infrastructure.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackRabbitConfig {
    public static final String EXCHANGE = "slack.exchange";
    public static final String QUEUE = "slack.send.queue";
    public static final String ROUTING_KEY = "slack.send";

    @Bean
    public DirectExchange slackExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue slackSendQueue() {
        // true는 RabbitMQ가 재시작되어도 큐를 유지하는 durable 설정
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding slackSendBinding(
            Queue slackSendQueue,
            DirectExchange slackExchange
    ) {
        return BindingBuilder
                .bind(slackSendQueue)
                .to(slackExchange)
                .with(ROUTING_KEY);
    }
}
