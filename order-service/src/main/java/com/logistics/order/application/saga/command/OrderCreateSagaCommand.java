package com.logistics.order.application.saga.command;

import com.logistics.order.application.dto.command.OrderCreateCommand;

import java.util.UUID;

public record OrderCreateSagaCommand(
        OrderCreateCommand orderCommand,
        UUID startCompanyId,
        UUID startHubId,
        UUID endHubId,
        String endCompanyAddress,
        String receiverName,
        String receiverSlackId,
        UUID idempotencyKey
) {
}
