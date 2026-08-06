package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventoryCreateResult;

import java.util.UUID;

public record InventoryCreateResponseDto(
        UUID inventoryId
) {
    public static InventoryCreateResponseDto from(
            InventoryCreateResult inventoryCreateResult
    ) {
        return new InventoryCreateResponseDto(
                inventoryCreateResult.inventoryId()
        );
    }
}
