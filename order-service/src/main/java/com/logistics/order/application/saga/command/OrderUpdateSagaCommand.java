package com.logistics.order.application.saga.command;

import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.domain.entity.Order;

import java.util.UUID;

public record OrderUpdateSagaCommand(
        Order order,
        OrderUpdateCommand orderUpdateCommand,
        UUID startHubId
) {
}
