package com.logistics.hubRoute.presentation.dto.dto.response;

import com.logistics.hubRoute.domain.entity.HubRoute;
import java.util.UUID;

public record HubRouteResponseDto(UUID hubRouteId) {

    public static HubRouteResponseDto from(HubRoute hub) {
        return new HubRouteResponseDto(hub.getHubRouteId());
    }
}
