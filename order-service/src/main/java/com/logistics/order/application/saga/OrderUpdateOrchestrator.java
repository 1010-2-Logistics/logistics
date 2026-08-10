package com.logistics.order.application.saga;


import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.command.OrderUpdateSagaCommand;
import com.logistics.order.application.dto.result.CompanyOrderInfoResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.port.CompanyPort;
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
        return null;
    }

    public void adjustInventory() {

    }

    public void compensateInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int quantityDifference,
            RuntimeException originalException
    ) {

    }

    private void deductInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int quantity
    ) {

    }

    private void restoreInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int quantity
    ) {

    }
}
