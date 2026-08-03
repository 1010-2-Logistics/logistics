package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventoryRestorationResultDto;

import java.util.UUID;

public record InventoryRestorationResponseDto(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
    public static InventoryRestorationResponseDto from(
            InventoryRestorationResultDto inventoryRestorationResultDto
    ) {
        return new InventoryRestorationResponseDto(
                inventoryRestorationResultDto.inventoryId(),
                inventoryRestorationResultDto.productId(),
                inventoryRestorationResultDto.stock()
        );
    }
}
