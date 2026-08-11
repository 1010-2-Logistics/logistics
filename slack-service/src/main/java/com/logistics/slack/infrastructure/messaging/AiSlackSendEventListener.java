package com.logistics.slack.infrastructure.messaging;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.UserInfo;
import com.logistics.slack.application.event.AiSlackSendEvent;
import com.logistics.slack.application.port.UserPort;
import com.logistics.slack.application.service.SlackCommandService;
import com.logistics.slack.infrastructure.config.SlackRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiSlackSendEventListener {
    // TODO 내부 시스템 발송 senderId 정책 확정 후 변경
    private static final Long SYSTEM_SENDER_ID = 0L;

    private final SlackCommandService slackCommandService;
    private final UserPort userPort;

    @RabbitListener(queues = SlackRabbitConfig.QUEUE)
    public void consume(AiSlackSendEvent event) {
        log.info(
                "AI Slack 이벤트 수신 - receiverId={}, referenceId={}",
                event.receiverId(),
                event.referenceId()
        );
        if (event.receiverId() == null) {
            log.error(
                    "AI Slack 이벤트 receiverId 누락 - event={}",
                    event
            );
            return;
        }

        UserInfo receiver = userPort.getUser(event.receiverId());

        SlackCreateCommand slackCreateCommand = new SlackCreateCommand(
//                senderId,
                SYSTEM_SENDER_ID,
                receiver.userId(),
                event.message(),
                event.referenceId()
        );

        slackCommandService.createSlack(
                slackCreateCommand,
                receiver.slackId()
        );
    }
}
