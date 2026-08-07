package com.logistics.delivery.presentation.controller.dto.response;

import com.logistics.delivery.application.dto.result.DeliveryResults.DeliveryInternalResult;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import java.util.UUID;

public record DeliveryInternalResponse(
        UUID deliveryId, DeliveryStatus status, UUID startHubId, UUID endHubId, Long deliveryManagerId
) {
    public static DeliveryInternalResponse from(DeliveryInternalResult result) {
        Delivery delivery = result.delivery();
        return new DeliveryInternalResponse(
                delivery.getDeliveryId(), delivery.getStatus(), delivery.getStartHubId(), delivery.getEndHubId(),
                result.currentManagerId());
    }
}