package com.logistics.delivery.application.dto.command;

import com.logistics.delivery.domain.entity.DeliveryStatus;

public record ChangeDeliveryStatusCommand(DeliveryStatus status) {
}