package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryGetOneResult(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        Integer stock,
        LocalDateTime createdAt
) {
    public static InventoryGetOneResult from(
            Inventory inventory
    ) {
        return new InventoryGetOneResult(inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getHubId(),
                inventory.getStock(),
                inventory.getCreatedAt()
        );
    }
}
