package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;

import java.util.UUID;

public record InventorySummaryResult(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        Integer stock
) {
    public static InventorySummaryResult from(
            Inventory inventory
    ) {
        return new InventorySummaryResult(
                inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getHubId(),
                inventory.getStock()
        );
    }
}