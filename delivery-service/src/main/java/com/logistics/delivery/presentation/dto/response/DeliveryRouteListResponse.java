package com.logistics.delivery.presentation.dto.response;

import com.logistics.delivery.application.dto.result.DeliveryResults.DeliveryRouteListResult;
import com.logistics.delivery.domain.entity.DeliveryRoute;
import com.logistics.delivery.domain.entity.DeliveryRouteStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DeliveryRouteListResponse(List<RouteItem> routes) {

    public record RouteItem(
            UUID deliveryRouteId, Integer sequence, Long deliveryManagerId,
            UUID startHubId, UUID endHubId, DeliveryRouteStatus status,
            BigDecimal expectedDistance, Integer expectedDuration,
            BigDecimal actualDistance, Integer actualDuration
    ) {
        public static RouteItem from(DeliveryRoute route) {
            return new RouteItem(
                    route.getDeliveryRouteId(), route.getSequence(), route.getDeliveryManagerId(),
                    route.getStartHubId(), route.getEndHubId(), route.getStatus(),
                    route.getExpectedDistance(), route.getExpectedDuration(),
                    route.getActualDistance(), route.getActualDuration());
        }
    }

    public static DeliveryRouteListResponse from(DeliveryRouteListResult result) {
        return new DeliveryRouteListResponse(result.routes().stream().map(RouteItem::from).toList());
    }
}