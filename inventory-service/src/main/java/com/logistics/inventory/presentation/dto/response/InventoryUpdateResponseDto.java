package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventoryUpdateResult;

import java.util.UUID;

public record InventoryUpdateResponseDto(
        UUID inventoryId // <- 명칭: record component
) {
    public static InventoryUpdateResponseDto from(
            InventoryUpdateResult inventoryUpdateResult
    ) {
        return new InventoryUpdateResponseDto(
                inventoryUpdateResult.inventoryId()
        );
    }
}
