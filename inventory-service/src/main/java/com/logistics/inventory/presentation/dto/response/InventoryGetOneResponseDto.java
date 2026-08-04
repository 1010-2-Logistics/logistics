package com.logistics.inventory.presentation.dto.response;


import com.logistics.inventory.application.dto.result.InventoryGetOneResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryGetOneResponseDto(
        UUID inventoryId,
        UUID productId,
        UUID hubId,
        Integer stock,
        LocalDateTime createdAt
) {
    public static InventoryGetOneResponseDto from(
            InventoryGetOneResult inventoryGetOneResult
    ) {
        return new InventoryGetOneResponseDto(
                inventoryGetOneResult.inventoryId(),
                inventoryGetOneResult.productId(),
                inventoryGetOneResult.hubId(),
                inventoryGetOneResult.stock(),
                inventoryGetOneResult.createdAt()
        );
    }
}
