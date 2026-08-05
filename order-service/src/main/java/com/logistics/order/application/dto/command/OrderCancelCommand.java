package com.logistics.order.application.dto.command;

import java.util.UUID;

public record OrderCancelCommand(
        UUID orderId
) {
}
