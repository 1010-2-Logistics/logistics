package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

interface DeliveryManagerAssignmentStateJpaRepository extends JpaRepository<DeliveryManagerAssignmentState, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DeliveryManagerAssignmentState> findByManagerTypeAndHubId(ManagerType managerType, UUID hubId);
}
