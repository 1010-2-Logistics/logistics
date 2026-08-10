package com.logistics.order.infrastructure.adapter;

import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.infrastructure.feign.client.InventoryClient;
import com.logistics.order.infrastructure.feign.request.InventoryDeductionRequest;
import com.logistics.order.infrastructure.feign.request.InventoryRestorationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryClientAdapter implements InventoryPort {
    private final InventoryClient inventoryClient;

    @Override
    public void deductInventory(
            UUID operationId,
            UUID orderId,
            UUID productId,
            UUID hubId,
            Integer quantity
    ) {
        InventoryDeductionRequest inventoryDeductionRequest = new InventoryDeductionRequest(
                operationId,
                orderId,
                productId,
                hubId,
                quantity
        );

        inventoryClient.deductInventory(inventoryDeductionRequest);
    }

    @Override
    public void restoreInventory(
            UUID operationId,
            UUID orderId,
            UUID productId,
            UUID hubId,
            Integer quantity
    ) {
        InventoryRestorationRequest inventoryRestorationRequest = new InventoryRestorationRequest(
                operationId,
                orderId,
                productId,
                hubId,
                quantity
        );

        inventoryClient.restoreInventory(inventoryRestorationRequest);
    }
}
