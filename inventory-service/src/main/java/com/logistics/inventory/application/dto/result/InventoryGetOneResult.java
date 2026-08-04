package com.logistics.inventory.application.dto.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryGetOneResult(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        Integer stock,
        LocalDateTime createdAt
) {
}
