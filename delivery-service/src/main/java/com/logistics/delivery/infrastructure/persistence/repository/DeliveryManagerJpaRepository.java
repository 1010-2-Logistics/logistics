package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DeliveryManagerJpaRepository extends JpaRepository<DeliveryManager, Long> {

    boolean existsByDeliveryManagerIdAndDeletedAtIsNull(Long deliveryManagerId);

    @Query("SELECT MAX(m.deliverySequence) FROM DeliveryManager m "
            + "WHERE m.managerType = :managerType "
            + "AND (:hubId IS NULL OR m.hubId = :hubId) "
            + "AND m.deletedAt IS NULL")
    Optional<Integer> findMaxSequence(@Param("managerType") ManagerType managerType, @Param("hubId") UUID hubId);
}