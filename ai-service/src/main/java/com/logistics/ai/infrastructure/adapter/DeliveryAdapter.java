package com.logistics.ai.infrastructure.adapter;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.internal.RouteInfo;
import com.logistics.ai.application.port.out.DeliveryPort;
import com.logistics.ai.infrastructure.feign.client.DeliveryClient;
import com.logistics.ai.infrastructure.feign.response.DeliveryRouteListResponseDto;
import com.logistics.ai.infrastructure.feign.response.DeliveryRouteListResponseDto.RouteItem;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryAdapter implements DeliveryPort {

	private final DeliveryClient deliveryClient;

	@Override
	public List<RouteInfo> getRoutes(UUID deliveryId) {
		DeliveryRouteListResponseDto response = deliveryClient.getRoutes(deliveryId).getData();
		
		List<RouteItem> routes = response.routes();
		
		return routes.stream()
				.sorted(Comparator.comparing(RouteItem::sequence, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(RouteItem::toApplication)
				.toList();
	}
}
