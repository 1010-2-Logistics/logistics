package com.logistics.order.application.saga;


import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.saga.command.OrderCancelSagaCommand;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelOrchestrator {
    private final OrderCommandService orderCommandService;
    private final DeliveryPort deliveryPort;
    private final InventoryPort inventoryPort;


    public OrderCancelResult execute(
            OrderCancelSagaCommand orderCancelSagaCommand
    ) {
        UUID operationId = UUID.randomUUID();

        cancelDelivery(
                orderCancelSagaCommand
        );

        restoreInventory(
                operationId,
                orderCancelSagaCommand
        );

        return cancelOrderWithCompensation(
                operationId,
                orderCancelSagaCommand
        );
    }

    public void cancelDelivery(
            OrderCancelSagaCommand orderCancelSagaCommand
    ) {
        deliveryPort.cancelDelivery(
                orderCancelSagaCommand.order().getOrderId()
        );
    }

    private void restoreInventory(
            UUID operationId,
            OrderCancelSagaCommand orderCancelSagaCommand
    ) {
        Order order = orderCancelSagaCommand.order();

        inventoryPort.restoreInventory(
                operationId,
                order.getOrderId(),
                order.getProductId(),
                orderCancelSagaCommand.startHubId(),
                order.getQuantity()
        );
    }

    private OrderCancelResult cancelOrderWithCompensation(
            UUID operationId,
            OrderCancelSagaCommand orderCancelSagaCommand
    ) {
        try {
            return orderCommandService.cancelOrder(
                    orderCancelSagaCommand.order()
            );

        } catch (RuntimeException originalException) {
            compensateInventoryDeduction(
                    operationId,
                    orderCancelSagaCommand,
                    originalException
            );
            throw originalException;
        }
    }

    private void compensateInventoryDeduction(
            UUID operationId,
            OrderCancelSagaCommand orderCancelSagaCommand,
            RuntimeException originalException
    ) {
        Order order = orderCancelSagaCommand.order();

        try {
            inventoryPort.deductInventory(
                    operationId,
                    order.getOrderId(),
                    order.getProductId(),
                    orderCancelSagaCommand.startHubId(),
                    order.getQuantity()
            );
        } catch (RuntimeException compensationException) {
            log.error(
                    "[ERROR Order] 주문 취소 보상 중 재고 재차감 실패. orderId={}, operationId={}",
                    order.getOrderId(),
                    operationId,
                    compensationException
            );

            originalException.addSuppressed(compensationException);
        }
    }
}