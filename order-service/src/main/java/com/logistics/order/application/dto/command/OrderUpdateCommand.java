package com.logistics.order.application.dto.command;

import java.util.UUID;

public record OrderUpdateCommand(
        UUID orderId,
        Integer quantity,
        String request
) {
}
