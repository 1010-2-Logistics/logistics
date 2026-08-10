package com.logistics.ai.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.logistics.ai.application.event.OrderCreatedEvent;
import com.logistics.ai.application.port.in.DispatchDeadlineUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

	private final DispatchDeadlineUseCase dispatchDeadlineUseCase;
	
	@RabbitListener(
			queues = "${rabbitmq.order-created.queue}",
			containerFactory = "orderCreatedRabbitListenerContainerFactory"
	)
	public void handle(OrderCreatedEvent event) {
		log.info("[AI-SERVICE] OrderCreatedEvent 수신, orderId = {}"
				, event.orderId()
		);
		
		dispatchDeadlineUseCase.generate(event);
	}
	
}
