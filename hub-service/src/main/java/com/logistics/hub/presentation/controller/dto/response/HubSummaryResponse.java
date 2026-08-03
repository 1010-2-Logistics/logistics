package com.logistics.hub.presentation.controller.dto.response;

import com.logistics.hub.domain.entity.Hub;
import java.util.UUID;

public record HubSummaryResponse(UUID sampleId, String name) {

    public static HubSummaryResponse from(Hub hub) {
        return new HubSummaryResponse(hub.getHubId(), hub.getHubName());
    }
}
