package com.logistics.order.infrastructure.feign.response;

import java.util.UUID;

public record DeliveryResponse(
        UUID deliveryId,
        String status,
        UUID startHubId,
        UUID endHubId,
        Long deliveryManagerId
) {
}
