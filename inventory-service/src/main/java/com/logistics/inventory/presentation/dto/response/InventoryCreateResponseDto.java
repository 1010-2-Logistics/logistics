package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventoryCreateResult;

import java.util.UUID;

public record InventoryCreateResponseDto(
        UUID inventoryId
) {
    public static InventoryCreateResponseDto from(
            InventoryCreateResult inventoryCreateResultDto
    ) {
        return new InventoryCreateResponseDto(
                inventoryCreateResultDto.inventoryId()
        );
    }
}
