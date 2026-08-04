package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;

import java.util.UUID;

public record InventoryDeductionResult(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
    public static InventoryDeductionResult from(
            Inventory inventory
    ) {
        return new InventoryDeductionResult(
                inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getStock()
        );
    }
}
