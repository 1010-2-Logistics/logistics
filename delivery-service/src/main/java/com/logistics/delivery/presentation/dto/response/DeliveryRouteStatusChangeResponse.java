package com.logistics.delivery.presentation.dto.response;

import com.logistics.delivery.application.dto.result.DeliveryResults;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryRoute;
import com.logistics.delivery.domain.entity.DeliveryRouteStatus;
import com.logistics.delivery.domain.entity.DeliveryStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryRouteStatusChangeResponse(UUID deliveryRouteId, DeliveryRouteStatus status, BigDecimal actualDistance, Integer actualDuration, DeliveryStatus deliveryStatus, Long companyDeliveryManagerId) {
    public static DeliveryRouteStatusChangeResponse of(DeliveryResults.RouteStatusChangeResult result) {
        DeliveryRoute route = result.route();
        Delivery delivery = result.delivery();
        return new DeliveryRouteStatusChangeResponse(route.getDeliveryRouteId(), route.getStatus(), route.getActualDistance(), route.getActualDuration(), delivery.getStatus(), delivery.getCompanyDeliveryManagerId());
    }
}