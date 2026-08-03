package com.logistics.hub.presentation.dto.dto.response;

import com.logistics.hub.domain.entity.Hub;
import java.util.UUID;

public record HubResponseDto(UUID hubId, String name, String status) {

    public static HubResponseDto from(Hub hub) {
        return new HubResponseDto(hub.getHubId(), hub.getHubName(), hub.getHubAddress());
    }
}
