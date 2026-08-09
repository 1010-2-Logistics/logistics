package com.logistics.inventory.application.dto.command;

import java.util.UUID;

public record InventoryRestorationCommand(
        UUID orderId,
        UUID productId,
        UUID hubId,
        Integer quantity
) {

}
