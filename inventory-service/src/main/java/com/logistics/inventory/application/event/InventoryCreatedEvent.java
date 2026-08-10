package com.logistics.inventory.application.event;

import java.util.UUID;

public record InventoryCreatedEvent(
        UUID inventoryId,
        String name
) {
}
