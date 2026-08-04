package com.logistics.order.infrastructure.feign.response;

import java.util.UUID;

public record InventoryRestorationResponse(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
}
