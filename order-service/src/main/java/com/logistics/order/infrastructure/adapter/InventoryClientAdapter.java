package com.logistics.order.infrastructure.adapter;

import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import com.logistics.order.infrastructure.feign.client.InventoryClient;
import com.logistics.order.infrastructure.feign.request.InventoryDeductionRequest;
import com.logistics.order.infrastructure.feign.request.InventoryRestorationRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
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

        try {
            InventoryDeductionRequest inventoryDeductionRequest = new InventoryDeductionRequest(
                    operationId,
                    orderId,
                    productId,
                    hubId,
                    quantity
            );
            inventoryClient.deductInventory(inventoryDeductionRequest);

        } catch (FeignException.Conflict e) {
            throw new CustomException(
                    OrderErrorCode.ORDER_OUT_OF_STOCK
            );
        } catch (FeignException e) {
            log.error(
                    "Inventory service 호출 실패. status={}, response={}",
                    e.status(),
                    e.contentUTF8(),
                    e
            );
            throw new CustomException(
                    OrderErrorCode.ORDER_SERVICE_UNAVAILABLE
            );
        }
    }

    @Override
    public void restoreInventory(
            UUID operationId,
            UUID orderId,
            UUID productId,
            UUID hubId,
            Integer quantity
    ) {
        try {
            InventoryRestorationRequest inventoryRestorationRequest = new InventoryRestorationRequest(
                    operationId,
                    orderId,
                    productId,
                    hubId,
                    quantity
            );

            inventoryClient.restoreInventory(inventoryRestorationRequest);
        } catch (FeignException e) {
            throw new CustomException(
                    OrderErrorCode.ORDER_SERVICE_UNAVAILABLE
            );
        }
    }
}
