package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventoryDeductionResult;

import java.util.UUID;

public record InventoryDeductionResponseDto(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
    public static InventoryDeductionResponseDto from(
            InventoryDeductionResult inventoryDeductionResultDto
    ) {
        return new InventoryDeductionResponseDto(
                inventoryDeductionResultDto.inventoryId(),
                inventoryDeductionResultDto.productId(),
                inventoryDeductionResultDto.stock()
        );
    }
}
