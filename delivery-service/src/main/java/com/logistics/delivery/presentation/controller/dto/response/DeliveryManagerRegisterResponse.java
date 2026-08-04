package com.logistics.delivery.presentation.controller.dto.response;

import com.logistics.delivery.domain.entity.DeliveryManager;

public record DeliveryManagerRegisterResponse(Long deliveryManagerId, Integer deliverySequence) {

    public static DeliveryManagerRegisterResponse from(DeliveryManager manager) {
        return new DeliveryManagerRegisterResponse(manager.getDeliveryManagerId(), manager.getDeliverySequence());
    }
}