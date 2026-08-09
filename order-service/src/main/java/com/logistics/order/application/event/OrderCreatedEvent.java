package com.logistics.order.application.event;

import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID deliveryId,
        UUID productId,
        Integer quantity
) {
}
