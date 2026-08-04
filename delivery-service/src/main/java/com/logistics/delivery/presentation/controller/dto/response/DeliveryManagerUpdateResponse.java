package com.logistics.delivery.presentation.controller.dto.response;

import com.logistics.delivery.domain.entity.DeliveryManager;
import java.util.UUID;

public record DeliveryManagerUpdateResponse(Long deliveryManagerId, UUID hubId) {

    public static DeliveryManagerUpdateResponse from(DeliveryManager manager) {
        return new DeliveryManagerUpdateResponse(manager.getDeliveryManagerId(), manager.getHubId());
    }
}