package com.logistics.order.infrastructure.persistence.repository;

import com.logistics.order.domain.entity.OutboxEvent;
import com.logistics.order.domain.entity.OutboxStatus;
import com.logistics.order.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {
    private final OutboxJpaRepository jpaRepository;

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return jpaRepository.save(outboxEvent);
    }

    @Override
    public List<OutboxEvent> findPendingEvents() {
        return jpaRepository
                .findTop100ByStatusOrderByCreatedAtAsc(
                        OutboxStatus.PENDING
                );
    }
}
