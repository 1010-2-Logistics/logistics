package com.logistics.inventory.application.dto.command;

import java.util.UUID;

public record InventoryDeductionCommand(
        UUID operationId,
        UUID orderId,
        UUID productId,
        UUID hubId,
        Integer quantity
) {

}
