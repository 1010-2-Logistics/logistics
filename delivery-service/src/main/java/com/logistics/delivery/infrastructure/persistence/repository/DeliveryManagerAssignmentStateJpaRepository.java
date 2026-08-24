package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

interface DeliveryManagerAssignmentStateJpaRepository extends JpaRepository<DeliveryManagerAssignmentState, Long> {

    // @Transactional 없으면 Spring Data가 기본 readOnly 트랜잭션을 씌우는데,
    // Postgres는 읽기전용 트랜잭션에서 SELECT ... FOR UPDATE를 거부한다.
    // 지금까지 항상 @Transactional 서비스 메서드 안에서만 호출돼 안 드러났을 뿐이다.
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DeliveryManagerAssignmentState> findByManagerTypeAndHubId(ManagerType managerType, UUID hubId);
}
