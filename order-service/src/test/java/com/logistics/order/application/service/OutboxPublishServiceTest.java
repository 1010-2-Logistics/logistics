package com.logistics.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.application.port.EventPublisher;
import com.logistics.order.domain.entity.OutboxEvent;
import com.logistics.order.domain.entity.OutboxStatus;
import com.logistics.order.domain.repository.OutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublishServiceTest {
    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxPublishService outboxPublishService;

    @Test
    @DisplayName("RabbitMQ 발행 성공 시 Outbox 상태 PUBLISHED로 변경")
    void publishPendingEvents_success() throws Exception {
        UUID orderId = UUID.randomUUID();

        OutboxEvent outboxEvent = OutboxEvent.create(
                orderId,
                "ORDER_CREATED",
                "{\"orderId\":\"" + orderId + "\"}"
        );
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                orderId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                10,
                "요청사항",
                "receiverName",
                "receiverSlackId",
                LocalDateTime.now()
        );

        given(outboxRepository.findPendingEvents()).willReturn(List.of(outboxEvent));
        given(objectMapper.readValue(
                outboxEvent.getPayload(),
                OrderCreatedEvent.class
        )).willReturn(orderCreatedEvent);
        given(eventPublisher.publish(
                orderCreatedEvent,
                outboxEvent.getEventId()
        )).willReturn(true);

        outboxPublishService.publishPendingEvents();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(outboxEvent.getPublishedAt()).isNotNull();

        verify(eventPublisher).publish(
                orderCreatedEvent,
                outboxEvent.getEventId()
        );
    }

    @Test
    @DisplayName("RabbitMQ 발행 실패 시 Outbox 상태 PENDING 유지")
    void publishPendingEvents_failureKeepsPending() throws Exception {
        UUID orderId = UUID.randomUUID();

        OutboxEvent outboxEvent = OutboxEvent.create(
                orderId,
                "ORDER_CREATED",
                "{\"orderId\":\"" + orderId + "\"}"
        );
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                orderId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                10,
                "요청사항",
                "receiverName",
                "receiverSlackId",
                LocalDateTime.now()
        );

        given(outboxRepository.findPendingEvents()).willReturn(List.of(outboxEvent));
        given(objectMapper.readValue(
                outboxEvent.getPayload(),
                OrderCreatedEvent.class
        )).willReturn(orderCreatedEvent);
        given(eventPublisher.publish(
                orderCreatedEvent,
                outboxEvent.getEventId()
        )).willReturn(false);

        outboxPublishService.publishPendingEvents();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEvent.getPublishedAt()).isNull();

        verify(eventPublisher).publish(
                orderCreatedEvent,
                outboxEvent.getEventId()
        );
    }

    @Test
    @DisplayName("RabbitMQ 발행 실패 후 재시도에 성공하면 Outbox 상태를 PUBLISHED로 변경")
    void publishPendingEvents_retrySuccess() throws Exception {
        UUID orderId = UUID.randomUUID();

        OutboxEvent outboxEvent = OutboxEvent.create(
                orderId,
                "ORDER_CREATED",
                "{\"orderId\":\"" + orderId + "\"}"
        );

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                orderId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                10,
                "요청사항",
                "receiverName",
                "receiverSlackId",
                LocalDateTime.now()
        );

        given(outboxRepository.findPendingEvents()).willReturn(List.of(outboxEvent));
        given(objectMapper.readValue(
                outboxEvent.getPayload(),
                OrderCreatedEvent.class
        )).willReturn(orderCreatedEvent);

        given(eventPublisher.publish(
                orderCreatedEvent,
                outboxEvent.getEventId()
        ))
                .willReturn(false)
                .willReturn(true);

        // 첫 번째 polling
        outboxPublishService.publishPendingEvents();

        // 실패했으므로 PENDING 유지
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEvent.getPublishedAt()).isNull();

        // 두 번째 polling
        outboxPublishService.publishPendingEvents();

        // 재시도 성공
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(outboxEvent.getPublishedAt()).isNotNull();

        verify(eventPublisher, times(2)).publish(
                orderCreatedEvent,
                outboxEvent.getEventId()
        );
    }

    @Test
    @DisplayName("Outbox payload 역직렬화 실패 시 이벤트를 발행하지 않고 PENDING 상태 유지")
    void publishPendingEvents_deserializeFailure() throws Exception {
        OutboxEvent outboxEvent = OutboxEvent.create(
                UUID.randomUUID(),
                "ORDER_CREATED",
                "invalid-json"
        );

        given(outboxRepository.findPendingEvents()).willReturn(List.of(outboxEvent));
        given(objectMapper.readValue(
                outboxEvent.getPayload(),
                OrderCreatedEvent.class
        )).willThrow(new JsonProcessingException("역직렬화 실패") {
        });

        assertThatThrownBy(() -> outboxPublishService.publishPendingEvents())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Outbox 이벤트 역직렬화에 실패했습니다.");

        verifyNoInteractions(eventPublisher);

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEvent.getPublishedAt()).isNull();
    }
}