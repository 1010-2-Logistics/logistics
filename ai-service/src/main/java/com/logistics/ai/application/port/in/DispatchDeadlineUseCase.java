package com.logistics.ai.application.port.in;

import java.util.UUID;

public interface DispatchDeadlineUseCase {
	void generate(UUID orderId, UUID deliveryId);
}
