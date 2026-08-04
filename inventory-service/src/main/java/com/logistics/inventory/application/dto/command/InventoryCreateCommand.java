package com.logistics.inventory.application.dto.command;

import java.util.UUID;

public record InventoryCreateCommand(
        UUID productId,
        UUID hubId,
        Integer stock
) {
}
