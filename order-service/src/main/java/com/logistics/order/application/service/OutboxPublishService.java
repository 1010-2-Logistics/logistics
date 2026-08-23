package com.logistics.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.application.port.EventPublisher;
import com.logistics.order.domain.entity.OutboxEvent;
import com.logistics.order.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxPublishService {
    private final OutboxRepository outboxRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishPendingEvents() {
        for (OutboxEvent outboxEvent : outboxRepository.findPendingEvents()) {
            OrderCreatedEvent orderCreatedEvent = deserialize(outboxEvent.getPayload());

            eventPublisher.publish(orderCreatedEvent);
            outboxEvent.markPublished();
        }
    }

    private OrderCreatedEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(
                    payload,
                    OrderCreatedEvent.class
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Outbox 이벤트 역직렬화에 실패했습니다.",
                    e
            );
        }
    }
}
