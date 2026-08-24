package com.logistics.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.application.port.EventPublisher;
import com.logistics.order.domain.entity.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxPublishService {
    private final OutboxTransactionService outboxTransactionService;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public void publishPendingEvents() {
        for (OutboxEvent outboxEvent : outboxTransactionService.findPendingEvents()) {
            OrderCreatedEvent event = deserialize(outboxEvent.getPayload());

            boolean published = eventPublisher.publish(
                    event,
                    outboxEvent.getEventId()
            );

            if (published) {
                outboxTransactionService.markPublished(
                        outboxEvent.getEventId()
                );
            }
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
