package com.logistics.order.infrastructure.messaging;

import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.infrastructure.config.MessagingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderEventPublisher orderEventPublisher;

    @Nested
    @DisplayName("이벤트 발행")
    class order_create_rabbitMQ {
        @Test
        @DisplayName("주문 생성 이벤트 RabbitMQ 발행 성공")
        void order_create_rabbitMQ_success() {
            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    3
            );

            orderEventPublisher.publish(orderCreatedEvent);

            verify(rabbitTemplate).convertAndSend(
                    MessagingConfig.ORDER_EXCHANGE,
                    MessagingConfig.ORDER_ROUTING_KEY,
                    orderCreatedEvent
            );
        }
    }
}