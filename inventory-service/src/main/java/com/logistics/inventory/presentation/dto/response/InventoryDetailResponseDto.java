package com.logistics.inventory.presentation.dto.response;


import com.logistics.inventory.application.dto.result.InventoryDetailResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryDetailResponseDto(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        Integer stock,
        LocalDateTime createdAt
) {
    public static InventoryDetailResponseDto from(
            InventoryDetailResult inventoryGetOneResult
    ) {
        return new InventoryDetailResponseDto(
                inventoryGetOneResult.inventoryId(),
                inventoryGetOneResult.productId(),
                inventoryGetOneResult.hubId(),
                inventoryGetOneResult.stock(),
                inventoryGetOneResult.createdAt()
        );
    }
}
