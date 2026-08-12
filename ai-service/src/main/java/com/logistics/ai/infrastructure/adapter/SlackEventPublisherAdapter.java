package com.logistics.ai.infrastructure.adapter;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.logistics.ai.application.event.SlackEvent;
import com.logistics.ai.application.port.out.SlackEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackEventPublisherAdapter implements SlackEventPublisher {
	
	private final RabbitTemplate template;
	
	@Value("${rabbitmq.slack.exchange}")
  private String slackExchange;

  @Value("${rabbitmq.slack.routing-key}")
  private String slackRoutingKey;
	
	@Override
	public void publish(SlackEvent event) {
		template.convertAndSend(
				slackExchange,
				slackRoutingKey,
				event
		);
	}
	
}
