package com.logistics.delivery.presentation.controller.dto.response;

import com.logistics.delivery.application.dto.result.DeliveryResults;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import java.util.UUID;

public record DeliverySummaryResponse(UUID deliveryId, UUID orderId, DeliveryStatus status, String deliveryAddress, String receiverName) {
    public static DeliverySummaryResponse from(DeliveryResults.DeliveryDetailResult result) {
        Delivery delivery = result.delivery();
        return new DeliverySummaryResponse(delivery.getDeliveryId(), delivery.getOrderId(), delivery.getStatus(), delivery.getDeliveryAddress(), delivery.getReceiverName());
    }
}