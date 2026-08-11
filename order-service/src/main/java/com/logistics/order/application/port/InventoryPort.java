package com.logistics.order.application.port;

import java.util.UUID;

public interface InventoryPort {
    void deductInventory(
            UUID operationId,
            UUID orderId,
            UUID productId,
            UUID hubId,
            Integer quantity
    );

    void restoreInventory(
            UUID operationId,
            UUID orderId,
            UUID productId,
            UUID hubId,
            Integer quantity
    );
}
