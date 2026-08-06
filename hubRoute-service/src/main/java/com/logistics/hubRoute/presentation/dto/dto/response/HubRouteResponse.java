package com.logistics.hubRoute.presentation.dto.dto.response;

import com.logistics.hubRoute.domain.entity.HubRoute;
import java.util.UUID;

public record HubRouteResponse(UUID hubRouteId) {

    public static HubRouteResponse from(HubRoute hub) {
        return new HubRouteResponse(hub.getHubRouteId());
    }
}
