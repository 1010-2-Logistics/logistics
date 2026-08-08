package com.logistics.ai.application.port.in;

import com.logistics.ai.application.event.OrderCreatedEvent;

public interface DispatchDeadlineUseCase {
	void generate(OrderCreatedEvent event);
}
