package com.logistics.inventory.application.port;

import com.logistics.inventory.application.dto.result.HubExistsResponseDto;

import java.util.UUID;

public interface HubPort {
    HubExistsResponseDto getHub(UUID hubId);
}
