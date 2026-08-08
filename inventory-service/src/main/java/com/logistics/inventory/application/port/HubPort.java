package com.logistics.inventory.application.port;

import com.logistics.inventory.application.dto.internal.response.HubExistsResponseDto;

import java.util.UUID;

public interface HubPort {
    HubExistsResponseDto getHub(UUID hubId);
}
