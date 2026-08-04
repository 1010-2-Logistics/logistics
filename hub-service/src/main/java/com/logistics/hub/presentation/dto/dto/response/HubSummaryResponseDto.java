package com.logistics.hub.presentation.dto.dto.response;

import com.logistics.hub.domain.entity.Hub;
import java.util.UUID;

public record HubSummaryResponseDto(UUID sampleId, String name) {

    public static HubSummaryResponseDto from(Hub hub) {
        return new HubSummaryResponseDto(hub.getHubId(), hub.getHubName());
    }
}
