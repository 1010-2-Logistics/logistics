package com.logistics.inventory.application.dto.command;

import com.logistics.inventory.presentation.dto.request.InventoryCreateRequestDto;

import java.util.UUID;

public record InventoryCreateCommand(
        UUID productId,
        UUID hubId,
        Integer stock
) {
    public static InventoryCreateCommand toCommand(
            InventoryCreateRequestDto inventoryCreateRequestDto
    ) {
        return new InventoryCreateCommand(
                inventoryCreateRequestDto.productId(),
                inventoryCreateRequestDto.hubId(),
                inventoryCreateRequestDto.stock()
        );
    }
}
