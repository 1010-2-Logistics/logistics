package com.logistics.inventory.application.dto.internal.response;

import java.util.UUID;

public record HubExistsResponseDto(
        UUID hubId,
        boolean exists
) {
}
