package com.logistics.order.application.saga;


import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.command.OrderUpdateSagaCommand;
import com.logistics.order.application.dto.result.CompanyOrderInfoResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.port.CompanyPort;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.infrastructure.feign.client.InventoryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderUpdateOrchestrator {

    private final OrderCommandService orderCommandService;
    private final InventoryClient inventoryClient;
    private final CompanyPort companyPort;
    private final InventoryPort inventoryPort;

    public OrderCreateResult execute(
            OrderUpdateSagaCommand orderUpdateSagaCommand
    ) {
        return null;
    }

    public OrderUpdateResult updateOrder(
            OrderUpdateSagaCommand orderUpdateSagaCommand
    ) {
        return orderCommandService.updateOrder(
                orderUpdateSagaCommand.order(),
                orderUpdateSagaCommand.orderUpdateCommand()
        );
    }

    public OrderUpdateResult updateOrderWithCompensation(
            UUID operationId,
            OrderUpdateSagaCommand orderUpdateSagaCommand,
            int quantityDifference
    ) {
        try {
            return updateOrder(orderUpdateSagaCommand);

        } catch (RuntimeException originException) {
            compensateInventory(
                    operationId,
                    orderUpdateSagaCommand.order(),
                    orderUpdateSagaCommand.startHubId(),
                    quantityDifference,
                    originException
            );
            throw originException;
        }
    }

    public void adjustInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int quantityDifference
    ) {
        if (quantityDifference > 0) {
            deductInventory(
                    operationId,
                    order,
                    hubId,
                    quantityDifference
            );
            return;
        }
        if (quantityDifference < 0) {
            restoreInventory(
                    operationId,
                    order,
                    hubId,
                    -quantityDifference
            );
        }
    }

    public void compensateInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int quantityDifference,
            RuntimeException originalException
    ) {
        try {
            if (quantityDifference > 0) {
                restoreInventory(
                        operationId,
                        order,
                        hubId,
                        quantityDifference
                );
                return;
            }
            if (quantityDifference < 0) {
                deductInventory(
                        operationId,
                        order,
                        hubId,
                        -quantityDifference
                );
            }
        } catch (RuntimeException compensationException) {
            log.error(
                    "[ERROR Order] 주문 수정 보상 중 재고 원복 실패. orderId={}, operationId={}",
                    order.getOrderId(),
                    operationId,
                    compensationException
            );

            originalException.addSuppressed(compensationException);
        }

    }

    private void deductInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int quantity
    ) {
        inventoryPort.deductInventory(
                operationId,
                order.getOrderId(),
                order.getProductId(),
                hubId,
                quantity
        );
    }

    private void restoreInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int quantity
    ) {
        inventoryPort.restoreInventory(
                operationId,
                order.getOrderId(),
                order.getProductId(),
                hubId,
                quantity
        );
    }
}
