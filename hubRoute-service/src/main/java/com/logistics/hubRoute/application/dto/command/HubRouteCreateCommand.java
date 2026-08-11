package com.logistics.hubRoute.application.dto.command;

import com.logistics.hubRoute.presentation.dto.dto.request.HubRouteCreateRequestDto;

import java.math.BigDecimal;
import java.util.UUID;

public record HubRouteCreateCommand(
        UUID startHubId,
        UUID endHubId,
        Integer duration,
        BigDecimal distance
) {
    public static HubRouteCreateCommand from(HubRouteCreateRequestDto dto) {
        return new HubRouteCreateCommand(
                dto.getStartHubId(),
                dto.getEndHubId(),
                dto.getDuration(),
                dto.getDistance()
        );
    }
}