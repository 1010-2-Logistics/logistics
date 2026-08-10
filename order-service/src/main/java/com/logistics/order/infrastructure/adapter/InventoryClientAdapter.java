package com.logistics.order.infrastructure.adapter;

import com.logistics.order.application.port.InventoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryClientAdapter implements InventoryPort {

    @Override
    public void deductInventory(
            UUID operationId,
            UUID orderId,
            UUID productId,
            UUID hubId,
            Integer quantity
    ) {

    }

    @Override
    public void restoreInventory(
            UUID operationId,
            UUID orderId,
            UUID productId,
            UUID hubId,
            Integer quantity
    ) {

    }
}
