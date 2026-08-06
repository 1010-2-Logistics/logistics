package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventoryListItemResult;

import java.util.UUID;

public record InventoryListItemResponseDto(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        Integer stock
) {
    public static InventoryListItemResponseDto from(
            InventoryListItemResult inventorySummaryResult
    ) {
        return new InventoryListItemResponseDto(
                inventorySummaryResult.inventoryId(),
                inventorySummaryResult.productId(),
                inventorySummaryResult.hubId(),
                inventorySummaryResult.stock()
        );
    }
}