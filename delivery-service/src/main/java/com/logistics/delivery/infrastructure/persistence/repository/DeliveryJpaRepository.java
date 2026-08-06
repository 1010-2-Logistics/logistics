package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DeliveryJpaRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByDeliveryIdAndDeletedAtIsNull(UUID deliveryId);

    @Query("SELECT d FROM Delivery d "
            + "WHERE d.deletedAt IS NULL "
            + "AND (:status IS NULL OR d.status = :status) "
            + "AND (:hubId IS NULL "
            + "     OR d.startHubId = :hubId OR d.endHubId = :hubId "
            + "     OR EXISTS (SELECT 1 FROM DeliveryRoute r "
            + "                WHERE r.deliveryId = d.deliveryId "
            + "                AND r.deletedAt IS NULL "
            + "                AND (r.startHubId = :hubId OR r.endHubId = :hubId)))")
    Page<Delivery> search(@Param("status") DeliveryStatus status, @Param("hubId") UUID hubId, Pageable pageable);
}