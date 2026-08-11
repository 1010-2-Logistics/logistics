package com.logistics.order.application.dto.result;

import java.util.UUID;

public record DeliveryGetResult(
        UUID deliveryId,
        UUID startHubId,
        UUID endHubId,
        Long deliveryManagerId
) {
}
