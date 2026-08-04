package com.logistics.delivery.application.dto.command;

import com.logistics.delivery.domain.entity.ManagerType;
import java.util.UUID;

public record RegisterDeliveryManagerCommand(
        Long userId,
        UUID hubId,
        String slackId,
        ManagerType managerType
) {
}