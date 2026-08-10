package com.logistics.delivery.presentation.dto.request;

import com.logistics.delivery.application.dto.command.ChangeDeliveryStatusCommand;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record DeliveryStatusChangeRequest(@NotNull DeliveryStatus status) {
    public ChangeDeliveryStatusCommand toCommand() {
        return new ChangeDeliveryStatusCommand(status);
    }
}