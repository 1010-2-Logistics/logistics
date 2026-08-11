package com.logistics.ai.infrastructure.messaging;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

import com.logistics.ai.application.event.OrderCreatedEvent;
import com.logistics.ai.application.port.in.DispatchDeadlineUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderCreatedEventListener {

	private final DispatchDeadlineUseCase dispatchDeadlineUseCase;
	
	private final OrderCreatedMessageRecover recover;
	
	private final RetryTemplate retryTemplate;
	
	public OrderCreatedEventListener(
			DispatchDeadlineUseCase dispatchDeadlineUseCase,
			OrderCreatedMessageRecover recover,
			@Qualifier("orderCreatedRetryTemplate") RetryTemplate retryTemplate
	) {
		this.dispatchDeadlineUseCase = dispatchDeadlineUseCase;
		this.recover = recover;
		this.retryTemplate = retryTemplate;
	}
	
	
	
	@RabbitListener(
			queues = "${rabbitmq.order-created.queue}",
			containerFactory = "rabbitListenerContainerFactory"
	)
	public void handle(OrderCreatedEvent event, Message message) {
		try {
			retryTemplate.execute(() -> {
				log.info("OrderCreatedEvent 수신, orderId: {}, deliveryId: {}", event.orderId(), event.deliveryId());
				
				dispatchDeadlineUseCase.generate(event);
				
				return null;
			});
		} catch (RetryException e) {
			Throwable cause = e.getCause() != null
					? e.getCause()
					: e;
			
			recover.recover(message, e.getRetryCount(), cause);
		}
	}
	
}
