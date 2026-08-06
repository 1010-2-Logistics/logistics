package com.logistics.delivery.application.dto.command;

import com.logistics.delivery.domain.entity.DeliveryRouteStatus;
import java.math.BigDecimal;

public record ChangeDeliveryRouteStatusCommand(
        DeliveryRouteStatus status, BigDecimal actualDistance, Integer actualDuration) {
}