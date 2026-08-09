package com.logistics.inventory.infrastructure.feign.response;

import java.util.UUID;

public record HubValidationResponse(
        UUID hubId,
        boolean exists
) {
}
