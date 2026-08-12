package com.logistics.slack.infrastructure.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackRabbitConfig {
    // AI -> Slack
    public static final String EXCHANGE = "slack.exchange";
    public static final String QUEUE = "slack.send.queue";
    public static final String ROUTING_KEY = "slack.send";

    // Slack 내부 실제 발송
    public static final String INTERNAL_QUEUE = "slack.dispatch.queue";
    public static final String INTERNAL_ROUTING_KEY = "slack.dispatch";

    @Bean
    public DirectExchange slackExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue slackSendQueue() {
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

    @Bean
    public Queue slackDispatchQueue() {
        return new Queue(INTERNAL_QUEUE, true);
    }

    @Bean
    public Binding slackDispatchBinding(
            Queue slackDispatchQueue,
            DirectExchange slackExchange
    ) {
        return BindingBuilder
                .bind(slackDispatchQueue)
                .to(slackExchange)
                .with(INTERNAL_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
