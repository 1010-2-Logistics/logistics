package com.logistics.ai.application.port.out;

import java.util.List;
import java.util.UUID;

import com.logistics.ai.application.dto.internal.RouteInfo;

public interface DeliveryPort {
	List<RouteInfo> getRoutes(UUID deliveryId);
}
