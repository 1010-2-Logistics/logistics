package com.logistics.slack.infrastructure.messaging;


import com.logistics.slack.application.event.SlackSendEvent;
import com.logistics.slack.application.port.SlackEventPublisher;
import com.logistics.slack.infrastructure.config.SlackRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitSlackEventPublisher  implements SlackEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(SlackSendEvent slackSendEvent) {
        rabbitTemplate.convertAndSend(
                SlackRabbitConfig.EXCHANGE,
                SlackRabbitConfig.INTERNAL_ROUTING_KEY,
                slackSendEvent
        );
    }
}
