package com.logistics.delivery.domain.repository;

import com.logistics.delivery.domain.entity.DeliveryRoute;

public interface DeliveryRouteRepository {
    DeliveryRoute save(DeliveryRoute deliveryRoute);
}