package com.logistics.delivery.presentation.dto.response;

import com.logistics.delivery.application.dto.result.DeliveryResults;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import java.util.UUID;

public record DeliveryResponse(UUID deliveryId, UUID orderId, UUID startHubId, UUID endHubId, DeliveryStatus status, Long companyDeliveryManagerId, String deliveryAddress, String receiverName, String receiverSlackId) {
    public static DeliveryResponse from(DeliveryResults.DeliveryDetailResult result) {
        Delivery delivery = result.delivery();
        return new DeliveryResponse(delivery.getDeliveryId(), delivery.getOrderId(), delivery.getStartHubId(), delivery.getEndHubId(), delivery.getStatus(), delivery.getCompanyDeliveryManagerId(), delivery.getDeliveryAddress(), delivery.getReceiverName(), delivery.getSlackId());
    }
}