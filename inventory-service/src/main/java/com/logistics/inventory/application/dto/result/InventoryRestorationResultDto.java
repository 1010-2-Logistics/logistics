package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;

import java.util.UUID;

public record InventoryRestorationResultDto(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
    public static InventoryRestorationResultDto from(
            Inventory inventory
    ) {
        return new InventoryRestorationResultDto(
                inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getStock()
        );
    }
}
