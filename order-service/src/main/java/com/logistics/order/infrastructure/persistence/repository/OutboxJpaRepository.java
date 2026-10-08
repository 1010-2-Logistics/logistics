package com.logistics.order.infrastructure.persistence.repository;

import com.logistics.order.domain.entity.OutboxEvent;
import com.logistics.order.domain.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface OutboxJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    // PENDING 상태인 Outbox 이벤트 중 오래된 것부터 최대 100개 조회
    // TODO - 배치 크기 100은 초기값이며, 부하 테스트와 운영 지표를 기반으로 조정
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(
            OutboxStatus status
    );
}
