package com.logistics.delivery.presentation.controller.dto.response;

import com.logistics.delivery.application.dto.result.DeliveryResults;
import java.util.UUID;

public record DeliveryCreateResponse(UUID deliveryId, int routeCount) {
    public static DeliveryCreateResponse from(DeliveryResults.DeliveryCreateResult result) {
        return new DeliveryCreateResponse(result.delivery().getDeliveryId(), result.routeCount());
    }
}