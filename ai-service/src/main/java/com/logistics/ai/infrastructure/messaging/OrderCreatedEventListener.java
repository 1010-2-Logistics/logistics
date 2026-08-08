package com.logistics.ai.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.logistics.ai.application.event.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

	@RabbitListener(queues = "${rabbitmq.order-created.queue}")
	public void handle(OrderCreatedEvent event) {
		
	}
	
}
