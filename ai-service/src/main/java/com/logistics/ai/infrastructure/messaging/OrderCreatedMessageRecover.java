package com.logistics.ai.infrastructure.messaging;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedMessageRecover implements MessageRecoverer {

	@Override
	public void recover(Message message, Throwable cause) {
		log.error("[AI-SERVICE]: OrderCreatedEvent 최종 처리 실패", cause);
		
		throw new AmqpRejectAndDontRequeueException("OrderCreatedEvent 이벤트 재시도 횟수 소진", cause);
	}

}
