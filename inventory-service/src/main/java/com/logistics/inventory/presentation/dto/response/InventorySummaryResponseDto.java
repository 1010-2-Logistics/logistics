package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventorySummaryResult;

import java.util.UUID;

public record InventorySummaryResponseDto(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        Integer stock
) {
    public static InventorySummaryResponseDto from(
            InventorySummaryResult inventorySummaryResult
    ) {
        return new InventorySummaryResponseDto(
                inventorySummaryResult.inventoryId(),
                inventorySummaryResult.productId(),
                inventorySummaryResult.hubId(),
                inventorySummaryResult.stock()
        );
    }
}