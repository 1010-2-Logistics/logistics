package com.logistics.order.infrastructure.feign.request;

import java.util.UUID;

public record InventoryRestorationRequest(
        UUID operationId,
        UUID orderId,
        UUID productId,
        UUID hubId,
        Integer quantity
) {
}
