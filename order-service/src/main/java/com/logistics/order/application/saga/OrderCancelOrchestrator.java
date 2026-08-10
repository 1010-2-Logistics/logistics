package com.logistics.order.application.saga;


import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.saga.command.OrderCancelSagaCommand;
import com.logistics.order.application.service.OrderCommandService;
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
    ){
        return null;
    }

    public void cancelDelivery(
            OrderCancelSagaCommand orderCancelSagaCommand
    ){

    }

    private void restoreInventory(
            UUID operationId,
            OrderCancelSagaCommand orderCancelSagaCommand
    ){

    }

    private OrderCancelResult cancelOrderWithCompensation(
            UUID operationId,
            OrderCancelSagaCommand orderCancelSagaCommand
    ){
        return null;
    }

    private void compensateInventoryDeduction(
            UUID operationId,
            OrderCancelSagaCommand orderCancelSagaCommand,
            RuntimeException originalException
    ){

    }


}
