package com.logistics.hubRoute.application.dto.query;

import java.math.BigDecimal;
import java.util.UUID;

public record HubRouteEdge(
        UUID startHubId,
        UUID endHubId,
        int duration,   // 소요 시간 (다익스트라 가중치 기준 1)
        BigDecimal distance // 거리 (다익스트라 가중치 기준 2)
) {
}
