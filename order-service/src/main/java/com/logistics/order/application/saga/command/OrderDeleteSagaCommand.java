package com.logistics.order.application.saga.command;

import com.logistics.order.domain.entity.Order;

import java.util.UUID;

public record OrderDeleteSagaCommand(
        Order order,
        UUID startHubId,
        Long deletedBy
) {
}
