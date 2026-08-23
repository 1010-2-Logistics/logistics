package com.logistics.order.domain.repository;

import com.logistics.order.domain.entity.OutboxEvent;

import java.util.List;

public interface OutboxRepository {
    OutboxEvent save(OutboxEvent outboxEvent);

    List<OutboxEvent> findPendingEvents();
}
