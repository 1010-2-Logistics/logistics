package com.logistics.delivery.presentation.controller.dto.request;

import com.logistics.delivery.application.dto.command.RegisterDeliveryManagerCommand;
import com.logistics.delivery.domain.entity.ManagerType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DeliveryManagerRegisterRequest(
        @NotNull Long userId,
        UUID hubId,
        @NotNull String slackId,
        @NotNull ManagerType managerType
) {
    public RegisterDeliveryManagerCommand toCommand() {
        return new RegisterDeliveryManagerCommand(userId, hubId, slackId, managerType);
    }
}