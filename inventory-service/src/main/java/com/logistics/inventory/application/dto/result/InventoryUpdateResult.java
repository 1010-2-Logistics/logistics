package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;

import java.util.UUID;

public record InventoryUpdateResult(
        UUID inventoryId
) {
    public static InventoryUpdateResult from(
            Inventory inventory
    ) {
        return new InventoryUpdateResult(
                inventory.getInventoryId()
        );
    }
}
