package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryRoute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

interface DeliveryRouteJpaRepository extends JpaRepository<DeliveryRoute, UUID> {
    int countByDeliveryIdAndDeletedAtIsNull(UUID deliveryId);
    Optional<DeliveryRoute> findByDeliveryRouteIdAndDeletedAtIsNull(UUID deliveryRouteId);
    List<DeliveryRoute> findAllByDeliveryIdAndDeletedAtIsNullOrderBySequenceAsc(UUID deliveryId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DeliveryRoute> findByDeliveryRouteIdAndDeletedAtIsNullForUpdate(UUID deliveryRouteId);
}