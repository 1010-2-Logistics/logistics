package com.logistics.delivery.domain.repository;

import com.logistics.delivery.domain.entity.DeliveryRoute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRouteRepository {
    DeliveryRoute save(DeliveryRoute deliveryRoute);
    int countByDeliveryId(UUID deliveryId);
    Optional<DeliveryRoute> findByIdAndDeletedAtIsNull(UUID deliveryRouteId);
    List<DeliveryRoute> findAllByDeliveryId(UUID deliveryId);
}