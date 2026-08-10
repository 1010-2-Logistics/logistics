package com.logistics.delivery.application.dto.result;

import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryRoute;

import java.util.List;

public final class DeliveryResults {

    private DeliveryResults() {
    }

    public record DeliveryCreateResult(Delivery delivery, int routeCount) {
    }

    public record RouteStatusChangeResult(DeliveryRoute route, Delivery delivery) {
    }

    public record DeliveryDetailResult(Delivery delivery) {
    }

    public record DeliveryRouteListResult(List<DeliveryRoute> routes) {
    }

    public record DeliveryInternalResult(Delivery delivery, Long currentManagerId) {
    }
}