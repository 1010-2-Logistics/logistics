package com.logistics.ai.application.event;

import java.util.UUID;

public record OrderCreatedEvent(
		UUID orderId,
		UUID deliveryId
) {

}
