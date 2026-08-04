package com.logistics.delivery.presentation.controller.dto.response;

import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import java.util.UUID;

public record DeliveryManagerResponse(Long deliveryManagerId, UUID hubId, ManagerType managerType, Integer deliverySequence) {

    public static DeliveryManagerResponse from(DeliveryManager manager) {
        return new DeliveryManagerResponse(
                manager.getDeliveryManagerId(), manager.getHubId(), manager.getManagerType(), manager.getDeliverySequence());
    }
}