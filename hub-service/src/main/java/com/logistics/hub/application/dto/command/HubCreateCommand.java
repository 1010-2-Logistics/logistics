package com.logistics.hub.application.dto.command;

import com.logistics.hub.presentation.dto.dto.request.HubCreateRequestDto;

import java.math.BigDecimal;

public record HubCreateCommand(
        String hubName,
        String hubAddress,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static HubCreateCommand from(HubCreateRequestDto dto) {
        return new HubCreateCommand(
                dto.getHubName(),
                dto.getHubAddress(),
                dto.getLatitude(),
                dto.getLongitude()
        );
    }
}