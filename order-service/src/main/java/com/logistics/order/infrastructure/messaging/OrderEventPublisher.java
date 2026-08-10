package com.logistics.order.infrastructure.messaging;

import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.application.port.EventPublisher;
import com.logistics.order.infrastructure.config.MessagingConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher implements EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                MessagingConfig.ORDER_EXCHANGE,
                MessagingConfig.ORDER_ROUTING_KEY,
                event
        );
    }
}
