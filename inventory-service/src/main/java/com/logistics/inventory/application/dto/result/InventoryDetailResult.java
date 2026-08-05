package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryDetailResult(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        Integer stock,
        LocalDateTime createdAt
) {
    public static InventoryDetailResult from(
            Inventory inventory
    ) {
        return new InventoryDetailResult(inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getHubId(),
                inventory.getStock(),
                inventory.getCreatedAt()
        );
    }
}
