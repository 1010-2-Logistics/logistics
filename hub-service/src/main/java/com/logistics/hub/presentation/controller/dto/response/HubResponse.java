package com.logistics.hub.presentation.controller.dto.response;

import com.logistics.hub.domain.entity.Hub;
import java.util.UUID;

public record HubResponse(UUID sampleId, String name, String status) {

    public static HubResponse from(Hub hub) {
        return new HubResponse(hub.getHubId(), hub.getHubName(), hub.getHubAddress());
    }
}
