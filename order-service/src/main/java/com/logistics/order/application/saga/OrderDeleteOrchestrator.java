package com.logistics.order.application.saga;


import com.logistics.order.application.saga.command.OrderDeleteSagaCommand;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDeleteOrchestrator {
    private final OrderCommandService orderCommandService;
    private final InventoryPort inventoryPort;

    public void execute(
            OrderDeleteSagaCommand orderDeleteSagaCommand
    ) {
        UUID operationId = UUID.randomUUID();

        restoreInventory(
                operationId,
                orderDeleteSagaCommand
        );

        deleteOrderWithCompensation(
                operationId,
                orderDeleteSagaCommand
        );
    }

    private void restoreInventory(
            UUID operationId,
            OrderDeleteSagaCommand orderDeleteSagaCommand
    ) {
        Order order = orderDeleteSagaCommand.order();

        inventoryPort.restoreInventory(
                operationId,
                order.getOrderId(),
                order.getProductId(),
                orderDeleteSagaCommand.startHubId(),
                order.getQuantity()
        );
    }

    private void deleteOrderWithCompensation(
            UUID operationId,
            OrderDeleteSagaCommand orderDeleteSagaCommand
    ) {
        try {
            orderCommandService.deleteOrder(
                    orderDeleteSagaCommand.order()
            );
        } catch (RuntimeException originException) {
            compensateInventoryDeduction(
                    operationId,
                    orderDeleteSagaCommand,
                    originException
            );

            throw originException;
        }
    }

    private void compensateInventoryDeduction(
            UUID operationId,
            OrderDeleteSagaCommand orderDeleteSagaCommand,
            RuntimeException originalException
    ) {
        Order order = orderDeleteSagaCommand.order();

        try {
            inventoryPort.deductInventory(
                    operationId,
                    order.getOrderId(),
                    order.getProductId(),
                    orderDeleteSagaCommand.startHubId(),
                    order.getQuantity()
            );
        } catch (RuntimeException compensationException) {
            log.error(
                    "[ERROR Order] 주문 삭제 보상 중 재고 재차감 실패. orderId={}, operationId={}",
                    order.getOrderId(),
                    operationId,
                    compensationException
            );

            originalException.addSuppressed(compensationException);
        }
    }
}
