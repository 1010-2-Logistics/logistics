package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;

import java.util.UUID;

public record InventoryDeductionResultDto(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
    public static InventoryDeductionResultDto from(
            Inventory inventory
    ) {
        return new InventoryDeductionResultDto(
                inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getStock()
        );
    }
}
