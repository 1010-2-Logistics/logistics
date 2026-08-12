package com.logistics.ai.infrastructure.adapter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.result.DlqRedriveResult;
import com.logistics.ai.application.event.OrderCreatedEvent;
import com.logistics.ai.application.port.in.OrderCreatedRedrive;
import com.logistics.ai.application.service.DispatchDeadlineQueryService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedRedriveAdapter implements OrderCreatedRedrive {
	
	private static final Duration PUBLISH_CONFIRM_TIMEOUT = Duration.ofSeconds(5);
	
	@Value("${rabbitmq.order-created.exchange}")
  private String orderCreatedExchange;

  @Value("${rabbitmq.order-created.queue}")
  private String orderCreatedQueue;

  @Value("${rabbitmq.order-created.dlq}")
  private String orderCreatedDlq;
	
	private final ConnectionFactory connectionFactory;
	
	private final DispatchDeadlineQueryService queryService;

	private final JsonMapper jsonMapper;
	
	@Override
	public DlqRedriveResult redrive(int count) {
		int redrivenCount = 0;
		int skippedCount = 0;
		int remainingMessageCount = 0;
		
    Connection connection = connectionFactory.createConnection();
    
    try(Channel channel = connection.createChannel(false)) {
    	channel.confirmSelect();

			AtomicBoolean returned = new AtomicBoolean(false);
			
			channel.addReturnListener(returnMessage -> returned.set(true));

    	for(int i = 0; i < count; i++) {
    		GetResponse dlqMessage = channel.basicGet(
            orderCreatedDlq,
            false
    		);
    		
    		if(dlqMessage == null) {
    			break;
    		}
    		
    		long deliveryTag = dlqMessage.getEnvelope().getDeliveryTag();
    		
    		returned.set(false);
    		
    		try {
				OrderCreatedEvent event = jsonMapper.readValue(
						dlqMessage.getBody(),
						OrderCreatedEvent.class
				);

				if(queryService.hasSucceeded(event.orderId())) {
					channel.basicAck(deliveryTag, false);
					skippedCount++;
					remainingMessageCount = dlqMessage.getMessageCount();
					log.info(
							"[AI-SERVICE]: redrive skipped, already succeeded, orderId = {}",
							event.orderId()
					);
					continue;
				}

    			channel.basicPublish(
    					orderCreatedExchange,
    					orderCreatedQueue,
    					true,
    					dlqMessage.getProps(),
    					dlqMessage.getBody()
    			);
    			
    			boolean confirmed = channel.waitForConfirms(PUBLISH_CONFIRM_TIMEOUT.toMillis());
    			
    			if(!confirmed || returned.get()) {
    				throw new IllegalStateException("Fail OrderCreatedEvent 를 원본 큐로 메시지를 발행하지 못했습니다.");
    			}
    			
    			channel.basicAck(deliveryTag, false);
          redrivenCount++;
          
          remainingMessageCount = dlqMessage.getMessageCount();
          
          log.info(
              "OrderCreatedEvent DLQ redrive 성공: messageId={}, redrivenCount={}",
              dlqMessage.getProps().getMessageId(),
              redrivenCount
          );
    		} catch (InterruptedException e) {
    			Thread.currentThread().interrupt();

          safeNack(channel, deliveryTag);

          throw new IllegalStateException(
                  "DLQ redrive 중 스레드가 중단되었습니다.",
                  e
          );
    		} catch (Exception e) {
    			safeNack(channel, deliveryTag);

          throw new IllegalStateException(
                  "DLQ redrive에 실패했습니다.",
                  e
          );
    		}
    	}
    	
    	return new DlqRedriveResult(
    			count,
    			redrivenCount,
				skippedCount,
    			remainingMessageCount
    	);
    } catch (IOException | TimeoutException e) {
    	throw new IllegalStateException(
          "RabbitMQ 채널 처리에 실패했습니다.",
          e
    	);
    } finally {
    	connection.close();
    }
	}
	
	private void safeNack(Channel channel, long deliveryTag) {
		if (!channel.isOpen()) {
      return;
		}
		
		try {
			channel.basicNack(deliveryTag, false, true);
		} catch (IOException e) {
			log.error(
					"DLQ 메시지 requeue 실패: deliveryTag = {}",
					deliveryTag,
					e
			);
		}
	}

}
