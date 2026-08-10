package com.logistics.ai.infrastructure.feign.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.logistics.ai.application.dto.internal.RouteInfo;

public record DeliveryRouteListResponseDto(List<RouteItem> routes) {

	public record RouteItem(
			UUID deliveryRouteId,
			Integer sequence,
			Long deliveryManagerId,
			UUID startHubId,
			UUID endHubId,
			DeliveryRouteStatus status,
			BigDecimal expectedDistance,
			Integer expectedDuration,
			BigDecimal actualDistance,
			Integer actualDuration
	) {
		public RouteInfo toApplication() {
			return new RouteInfo(
					this.sequence,
					this.startHubId,
					this.endHubId,
					this.expectedDistance,
					this.expectedDuration
			);
		}
	}
	
	public enum DeliveryRouteStatus {
		HUB_MOVE_WAITING,
    HUB_MOVING,
    DEST_HUB_ARRIVED
	}
	
}