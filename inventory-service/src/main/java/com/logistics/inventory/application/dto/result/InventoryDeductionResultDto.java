package com.logistics.inventory.application.dto.result;

import java.util.UUID;

public record InventoryDeductionResultDto(
        UUID inventoryId,
        UUID productId,
        Integer stock
) {
}
