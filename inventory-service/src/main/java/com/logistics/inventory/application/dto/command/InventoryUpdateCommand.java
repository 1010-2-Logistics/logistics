package com.logistics.inventory.application.dto.command;

import com.logistics.inventory.presentation.dto.request.InventoryUpdateRequestDto;

import java.util.UUID;

public record InventoryUpdateCommand(
        UUID inventoryId,
        Integer stock
) {
    public static InventoryUpdateCommand from(
            UUID inventoryId,
            InventoryUpdateRequestDto inventoryUpdateRequestDto
    ) {
        return new InventoryUpdateCommand(
                inventoryId,
                inventoryUpdateRequestDto.stock()
        );
    }
}
