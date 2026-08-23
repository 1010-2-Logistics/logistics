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
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


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
        @DisplayName("RabbitMQ ACK 수신 시 이벤트 발행 성공")
        void publish_success_whenAckReceived() {
            UUID eventId = UUID.randomUUID();

            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    10,
                    "요청사항",
                    "receiverName",
                    "receiverSlackId",
                    LocalDateTime.now()
            );

            doAnswer(invocation -> {
                CorrelationData correlationData = invocation.getArgument(3);
                correlationData.getFuture().complete(new CorrelationData.Confirm(
                                true,
                                null
                        )
                );

                return null;
            }).when(rabbitTemplate).convertAndSend(
                    eq(MessagingConfig.ORDER_EXCHANGE),
                    eq(MessagingConfig.ORDER_ROUTING_KEY),
                    eq(orderCreatedEvent),
                    any(CorrelationData.class)
            );

            boolean result = orderEventPublisher.publish(
                    orderCreatedEvent,
                    eventId
            );

            assertThat(result).isTrue();

            verify(rabbitTemplate).convertAndSend(
                    eq(MessagingConfig.ORDER_EXCHANGE),
                    eq(MessagingConfig.ORDER_ROUTING_KEY),
                    eq(orderCreatedEvent),
                    any(CorrelationData.class)
            );
        }

        @Test
        @DisplayName("RabbitMQ NACK 수신 시 이벤트 발행에 실패한다")
        void publish_failure_whenNackReceived() {
            UUID eventId = UUID.randomUUID();

            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    10,
                    "요청사항",
                    "receiverName",
                    "receiverSlackId",
                    LocalDateTime.now()
            );

            doAnswer(invocation -> {
                CorrelationData correlationData = invocation.getArgument(3);
                correlationData.getFuture().complete(new CorrelationData.Confirm(
                                false,
                                "broker nack"
                        )
                );

                return null;
            }).when(rabbitTemplate).convertAndSend(
                    eq(MessagingConfig.ORDER_EXCHANGE),
                    eq(MessagingConfig.ORDER_ROUTING_KEY),
                    eq(orderCreatedEvent),
                    any(CorrelationData.class)
            );

            boolean result = orderEventPublisher.publish(
                    orderCreatedEvent,
                    eventId
            );

            assertThat(result).isFalse();

            verify(rabbitTemplate).convertAndSend(
                    eq(MessagingConfig.ORDER_EXCHANGE),
                    eq(MessagingConfig.ORDER_ROUTING_KEY),
                    eq(orderCreatedEvent),
                    any(CorrelationData.class)
            );
        }

        @Test
        @DisplayName("RabbitMQ Publisher Confirm 응답이 시간 내 도착하지 않으면 이벤트 발행 실패")
        void publish_failure_whenConfirmTimeout() {
            UUID eventId = UUID.randomUUID();

            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    10,
                    "요청사항",
                    "receiverName",
                    "receiverSlackId",
                    LocalDateTime.now()
            );

            // CorrelationData의 Future를 일부러 완료시키지 않는다
            // → ACK도 NACK도 오지 않는 상황을 재현
            doNothing().when(rabbitTemplate).convertAndSend(
                    eq(MessagingConfig.ORDER_EXCHANGE),
                    eq(MessagingConfig.ORDER_ROUTING_KEY),
                    eq(orderCreatedEvent),
                    any(CorrelationData.class)
            );

            boolean result = orderEventPublisher.publish(
                    orderCreatedEvent,
                    eventId
            );

            assertThat(result).isFalse();

            verify(rabbitTemplate).convertAndSend(
                    eq(MessagingConfig.ORDER_EXCHANGE),
                    eq(MessagingConfig.ORDER_ROUTING_KEY),
                    eq(orderCreatedEvent),
                    any(CorrelationData.class)
            );
        }
    }
}