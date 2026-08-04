package com.logistics.order.application.dto.command;


import java.util.UUID;

public record OrderCreateCommand(
        UUID startCompanyId,
        UUID endCompanyId,
        UUID productId,
        Integer quantity,
        String request
) {
}
