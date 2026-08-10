package com.logistics.ai.infrastructure.messaging;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import com.logistics.ai.application.event.OrderCreatedEvent;
import com.logistics.ai.application.port.in.DispatchDeadlineCommandService;
import com.logistics.ai.infrastructure.exception.DeadlineGenerationRetryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedMessageRecover {

	private final DispatchDeadlineCommandService commandService;
	
	private final JsonMapper jsonMapper;
	
	public void recover(Message message, int retryCount, Throwable cause) {
		try {
			OrderCreatedEvent event = jsonMapper.readValue(
					message.getBody(),
					OrderCreatedEvent.class
			);
			
			DeadlineGenerationRetryException aiFailure = findAiFailure(cause);
			
			int actualRetryCount = aiFailure != null
					? aiFailure.getRetryCount()
					: retryCount;
			
			String requestPrompt = aiFailure != null
					? aiFailure.getRequestPrompt()
					: null;
			
			String aiModel = aiFailure != null
					? aiFailure.getAiModel()
					: null;
			
			String callMessage = getRootMessage(cause);
			
			commandService.saveFailed(
					event.orderId(),
					event.deliveryId(),
					requestPrompt,
					aiModel,
					callMessage,
					actualRetryCount
			);
			
			log.error("[AI-SERVICE]: OrderCreatedEvent 최종 실패 이력 저장, orderId = {}, retryCount = {}, cause = {}",
					event.orderId(),
					actualRetryCount,
					callMessage,
					cause
			);			
			
		} catch(Exception recoverException) {
			log.error("[AI-SERVICE] OrderCreatedEvent 실패 이력 저장 실패", recoverException);
		}
		
		throw new AmqpRejectAndDontRequeueException("OrderCreatedEvent 최종 처리 실패", cause);
	}

	private DeadlineGenerationRetryException findAiFailure(Throwable cause) {
		Throwable current = cause;
		
		while(current != null) {
			if(current instanceof DeadlineGenerationRetryException exception) {
				return exception;
			}
			
			if(current == current.getCause()) {
				break;
			}
			
			current = current.getCause();
		}
		
		return null;
	}

	private String getRootMessage(Throwable t) {
		if(t == null) {
			return "처리 실패 원인을 찾을 수 없습니다.";
		}
		
		Throwable rootCause = t;
		
		while(rootCause.getCause() != null && rootCause.getCause() != rootCause) {
			rootCause = rootCause.getCause();
		}
		String message = rootCause.getMessage();
		
		return message == null || message.isBlank()
				? rootCause.getClass().getSimpleName()
				: message;
	}

}
