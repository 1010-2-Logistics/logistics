package com.logistics.delivery.application.dto.command;

import java.util.UUID;

public record CreateDeliveryCommand(
        UUID orderId,
        UUID startHubId,
        UUID endHubId,
        String deliveryAddress,
        String receiverName,
        String slackId
) {
}