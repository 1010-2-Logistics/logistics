package com.logistics.order.infrastructure.feign.response;

import java.util.UUID;

public record InventoryDeductionResponse(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
}
