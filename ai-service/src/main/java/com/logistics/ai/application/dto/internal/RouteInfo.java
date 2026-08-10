package com.logistics.ai.application.dto.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RouteInfo(
		Integer sequence,
		UUID startHubId,
		UUID endHubId,
		BigDecimal expectedDistance,
		Integer expectedDuration
) {
	
}
