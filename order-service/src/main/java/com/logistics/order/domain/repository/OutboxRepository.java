package com.logistics.order.domain.repository;

import com.logistics.order.domain.entity.OutboxEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxRepository {
    OutboxEvent save(OutboxEvent outboxEvent);

    List<OutboxEvent> findPendingEvents();

    Optional<OutboxEvent> findById(UUID eventId);
}
