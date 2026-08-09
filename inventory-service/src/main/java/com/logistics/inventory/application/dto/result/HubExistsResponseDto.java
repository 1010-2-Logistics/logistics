package com.logistics.inventory.application.dto.result;

import java.util.UUID;

public record HubExistsResponseDto(
        UUID hubId,
        boolean exists
) {
}
