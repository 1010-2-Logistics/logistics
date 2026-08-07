package com.logistics.delivery.presentation.controller.dto.response;

import com.logistics.delivery.application.dto.result.DeliveryResults;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;

import java.util.UUID;

public record DeliveryStatusChangeResponse(UUID deliveryId, DeliveryStatus status) {
    public static DeliveryStatusChangeResponse from(DeliveryResults.DeliveryDetailResult result) {
        return new DeliveryStatusChangeResponse(result.delivery().getDeliveryId(), result.delivery().getStatus());
    }
}