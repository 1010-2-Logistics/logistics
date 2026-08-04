package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;

import java.util.UUID;

public record InventoryRestorationResult(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
    public static InventoryRestorationResult from(
            Inventory inventory
    ) {
        return new InventoryRestorationResult(
                inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getStock()
        );
    }
}
