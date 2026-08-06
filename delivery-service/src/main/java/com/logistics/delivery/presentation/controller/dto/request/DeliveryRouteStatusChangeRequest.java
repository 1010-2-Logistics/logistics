package com.logistics.delivery.presentation.controller.dto.request;

import com.logistics.delivery.application.dto.command.ChangeDeliveryRouteStatusCommand;
import com.logistics.delivery.domain.entity.DeliveryRouteStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DeliveryRouteStatusChangeRequest(
        @NotNull DeliveryRouteStatus status, BigDecimal actualDistance, Integer actualDuration) {
    public ChangeDeliveryRouteStatusCommand toCommand() {
        return new ChangeDeliveryRouteStatusCommand(status, actualDistance, actualDuration);
    }
}