package com.logistics.inventory.application.dto.internal.response;

import java.util.UUID;

public record ProductExistsResponseDto(
        UUID productId,
        boolean exists
) {
}
