package com.logistics.order.application.saga;


import com.logistics.order.application.dto.command.OrderDeleteSagaCommand;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.service.OrderCommandService;
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
    ){

    }

    private void restoreInventory(
            UUID operationId,
            OrderDeleteSagaCommand orderDeleteSagaCommand
    ){

    }

    private void deleteOrderWithCompensation(
        UUID operationId,
        OrderDeleteSagaCommand orderDeleteSagaCommand
    ){

    }

    private void compensateInventoryDeduction(
            UUID operationId,
            OrderDeleteSagaCommand orderDeleteSagaCommand,
            RuntimeException originalException
    ){

    }
}
