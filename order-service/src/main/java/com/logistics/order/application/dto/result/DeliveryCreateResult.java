package com.logistics.order.application.dto.result;

import java.util.UUID;

public record DeliveryCreateResult(
        UUID deliveryId,
        Integer routeCount
) {
}
