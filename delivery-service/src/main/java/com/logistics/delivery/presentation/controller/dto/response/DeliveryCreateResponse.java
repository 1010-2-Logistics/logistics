package com.logistics.delivery.presentation.controller.dto.response;

import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import java.util.UUID;

public record DeliveryCreateResponse(UUID deliveryId, DeliveryStatus status) {
    public static DeliveryCreateResponse from(Delivery delivery) {
        return new DeliveryCreateResponse(delivery.getDeliveryId(), delivery.getStatus());
    }
}