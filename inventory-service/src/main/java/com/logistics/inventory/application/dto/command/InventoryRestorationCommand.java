package com.logistics.inventory.application.dto.command;

import java.util.UUID;

public record InventoryRestorationCommand(
        UUID productId,
        UUID hubId,
        Integer stock
) {

}
