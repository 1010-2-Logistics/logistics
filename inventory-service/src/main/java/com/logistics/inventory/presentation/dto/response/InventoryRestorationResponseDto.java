package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventoryRestorationResult;

import java.util.UUID;

public record InventoryRestorationResponseDto(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
    public static InventoryRestorationResponseDto from(
            InventoryRestorationResult inventoryRestorationResultDto
    ) {
        return new InventoryRestorationResponseDto(
                inventoryRestorationResultDto.inventoryId(),
                inventoryRestorationResultDto.productId(),
                inventoryRestorationResultDto.stock()
        );
    }
}
