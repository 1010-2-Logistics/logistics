package com.logistics.order.application.dto.command;

import com.logistics.order.domain.entity.Order;

import java.util.UUID;

public record OrderUpdateSagaCommand(
        Order order,
        OrderUpdateCommand orderUpdateCommand,
        UUID startHubId
) {
}
