package com.logistics.delivery.application.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface HubRoutePort {

    List<HubRouteSegment> findRoute(UUID startHubId, UUID endHubId);

    record HubRouteSegment(UUID startHubId, UUID endHubId, BigDecimal distance, Integer duration) {
    }
}