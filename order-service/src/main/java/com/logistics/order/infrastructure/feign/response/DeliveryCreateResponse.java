package com.logistics.order.infrastructure.feign.response;

import java.util.UUID;

public record DeliveryCreateResponse(
        UUID deliveryId,
        Integer routeCount
) {
}
