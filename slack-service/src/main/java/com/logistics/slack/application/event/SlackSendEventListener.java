package com.logistics.slack.application.event;


import com.logistics.slack.application.service.SlackCommandService;
import com.logistics.slack.infrastructure.config.SlackRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SlackSendEventListener {
    private final SlackCommandService slackCommandService;

    @RabbitListener(queues = SlackRabbitConfig.INTERNAL_QUEUE)
    public void consume(SlackSendEvent slackSendEvent) {
        slackCommandService.send(
                slackSendEvent.slackMessageId(),
                // TODO AI 연동 후 receiverSlackId 추가 예정
                slackSendEvent.receiverSlackId()
        );
    }
}
