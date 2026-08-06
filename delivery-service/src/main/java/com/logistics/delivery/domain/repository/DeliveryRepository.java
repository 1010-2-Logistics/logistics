package com.logistics.delivery.domain.repository;

import com.logistics.delivery.domain.entity.Delivery;

public interface DeliveryRepository {
    Delivery save(Delivery delivery);

}