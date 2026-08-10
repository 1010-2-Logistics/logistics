package com.logistics.inventory.application.dto.command;

import java.util.UUID;

public record InventoryDeleteCommand(
        UUID inventoryId
) {
}
