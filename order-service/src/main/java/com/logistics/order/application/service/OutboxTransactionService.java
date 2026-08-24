package com.logistics.order.application.service;


import com.logistics.order.domain.entity.OutboxEvent;
import com.logistics.order.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxTransactionService {
    private final OutboxRepository outboxRepository;

    @Transactional(readOnly = true)
    public List<OutboxEvent> findPendingEvents() {
        return outboxRepository.findPendingEvents();
    }

    @Transactional
    public void markPublished(UUID eventId) {
        OutboxEvent outboxEvent = outboxRepository.findById(eventId).orElseThrow();

        outboxEvent.markPublished();
    }
}
