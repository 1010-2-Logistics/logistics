package com.logistics.delivery.presentation.controller.dto.request;

import com.logistics.delivery.application.dto.command.CreateDeliveryCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DeliveryCreateRequest(
        @NotNull UUID orderId,
        @NotNull UUID startHubId,
        @NotNull UUID endHubId,
        @NotBlank String deliveryAddress,
        @NotBlank String receiverName,
        @NotBlank String slackId
) {
    public CreateDeliveryCommand toCommand() {
        return new CreateDeliveryCommand(orderId, startHubId, endHubId, deliveryAddress, receiverName, slackId);
    }
}