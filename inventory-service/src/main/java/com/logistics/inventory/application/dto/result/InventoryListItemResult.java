package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;

import java.util.UUID;

public record InventoryListItemResult(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        Integer stock
) {
    public static InventoryListItemResult from(
            Inventory inventory
    ) {
        return new InventoryListItemResult(
                inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getHubId(),
                inventory.getStock()
        );
    }
}