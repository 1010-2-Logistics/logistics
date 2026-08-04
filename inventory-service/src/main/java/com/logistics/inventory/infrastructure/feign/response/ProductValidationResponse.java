package com.logistics.inventory.infrastructure.feign.response;

import java.util.UUID;

public record ProductValidationResponse(
    UUID productId,
    boolean exists
) {
}
