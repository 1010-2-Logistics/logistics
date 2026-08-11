package com.logistics.slack.application.event;


import com.logistics.slack.application.dto.result.UserInfo;
import com.logistics.slack.application.port.UserPort;
import com.logistics.slack.application.service.SlackCommandService;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackQueryRepository;
import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.global.exception.SlackErrorCode;
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
    private final SlackQueryRepository slackQueryRepository;
    private final UserPort userPort;

    @RabbitListener(
            queues = SlackRabbitConfig.QUEUE
    )
    public void consume(
            SlackSendEvent slackSendEvent
    ) {
        Slack slack = slackQueryRepository
                .findByIdAndDeletedAtIsNull(slackSendEvent.slackMessageId())
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_NOT_FOUND));

        UserInfo receiver = userPort.getUser(slack.getReceiverId());

        slackCommandService.send(
                slack.getSlackMessageId(),
                receiver.slackId()
        );
    }
}
