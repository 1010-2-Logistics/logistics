package com.logistics.inventory.application.dto.command;

import java.util.UUID;

public record InventoryDeductionCommand(
        UUID productId,
        UUID hubId,
        Integer quantity
) {

}
