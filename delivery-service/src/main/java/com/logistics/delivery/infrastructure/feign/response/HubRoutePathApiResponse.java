package com.logistics.delivery.infrastructure.feign.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record HubRoutePathApiResponse(
        @JsonProperty("SUCCESS") boolean success,
        int code,
        HubRoutePathResponse data,
        String message
) {
    public record HubRoutePathResponse(
            UUID startHubId, UUID endHubId, int totalDuration, BigDecimal totalDistance, List<Step> steps
    ) {
        public record Step(int sequence, UUID startHubId, UUID endHubId, int duration, BigDecimal distance) {
        }
    }
}