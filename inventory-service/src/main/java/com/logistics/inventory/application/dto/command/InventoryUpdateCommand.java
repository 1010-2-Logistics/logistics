package com.logistics.inventory.application.dto.command;

import com.logistics.inventory.presentation.dto.request.InventoryUpdateRequestDto;

public record InventoryUpdateCommand(
        Integer stock
) {
    public static InventoryUpdateCommand toCommand(
            InventoryUpdateRequestDto inventoryUpdateRequestDto
    ) {
        return new InventoryUpdateCommand(
                inventoryUpdateRequestDto.stock()
        );
    }
}
