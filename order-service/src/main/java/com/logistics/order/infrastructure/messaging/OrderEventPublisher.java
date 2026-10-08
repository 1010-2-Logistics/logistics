package com.logistics.order.infrastructure.messaging;

import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.application.port.EventPublisher;
import com.logistics.order.infrastructure.config.MessagingConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher implements EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public boolean publish(
            OrderCreatedEvent orderCreatedEvent,
            UUID eventId
    ) {
        CorrelationData correlationData = new CorrelationData(
                eventId.toString()
        );

        rabbitTemplate.convertAndSend(
                MessagingConfig.ORDER_EXCHANGE,
                MessagingConfig.ORDER_ROUTING_KEY,
                orderCreatedEvent,
                correlationData
        );
        try {
            CorrelationData.Confirm confirm = correlationData.getFuture().get(
                    5,
                    TimeUnit.SECONDS
            );

            return confirm.ack();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;

        } catch (ExecutionException | TimeoutException e) {
            return false;
        }
    }

    @Override
    public void publish(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                MessagingConfig.ORDER_EXCHANGE,
                MessagingConfig.ORDER_ROUTING_KEY,
                event
        );
    }
}
