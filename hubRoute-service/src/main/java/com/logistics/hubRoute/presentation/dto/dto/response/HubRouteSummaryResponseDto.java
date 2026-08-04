package com.logistics.hubRoute.presentation.dto.dto.response;

import com.logistics.hubRoute.domain.entity.HubRoute;
import java.util.UUID;

public record HubRouteSummaryResponseDto(UUID sampleId) {

    public static HubRouteSummaryResponseDto from(HubRoute hub) {
        return new HubRouteSummaryResponseDto(hub.getHubRouteId());
    }
}
