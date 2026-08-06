package com.logistics.slack.application.service;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.application.dto.result.SlackRetryResult;
import com.logistics.slack.application.event.SlackSendEvent;
import com.logistics.slack.application.port.SlackMessageSender;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackCommandRepository;

import java.util.UUID;

import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.global.exception.SlackErrorCode;
import com.logistics.slack.infrastructure.messaging.SlackEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SlackCommandService {
    private static final int MAX_RETRY_COUNT = 3;
    private final SlackCommandRepository slackCommandRepository;
    private final SlackEventPublisher slackEventPublisher;

    // TODO : RabbitMQ에 Slack 발송 이벤트를 발행하고,
    // Slack 이벤트 리스너에서 Webhook 발송 및 SUCCESS/FAILED 상태 변경 처리
    private final SlackMessageSender slackMessageSender;

    public SlackCreateResult createSlack(
            SlackCreateCommand slackCreateCommand
    ) {
        Slack slack = Slack.create(
                slackCreateCommand.senderId(),
                slackCreateCommand.receiverId(),
                slackCreateCommand.message(),
                slackCreateCommand.referenceId()
        );
        slackCommandRepository.save(slack);

        slackEventPublisher.publish(
                new SlackSendEvent(slack.getSlackMessageId())
        );
        // 이 시점의 slack.status는 아직 PENDING!
        return SlackCreateResult.from(slack);
    }

    public void send(
            UUID slackMessageId
    ) {
        Slack slack = slackCommandRepository
                .findByIdAndDeletedAtIsNull(slackMessageId)
                .orElseThrow();

        try {
            slackMessageSender.send(slack.getMessage());
            slack.markSuccess();

        } catch (Exception e) {
            slack.markFailed(e.getMessage());
        }
    }

    public SlackRetryResult retrySlack(
            UUID slackMessageId
    ) {
        Slack slack = slackCommandRepository.findById(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_NOT_FOUND));

        slack.retry(MAX_RETRY_COUNT);

        slackEventPublisher.publish(new SlackSendEvent(slack.getSlackMessageId()));

        return SlackRetryResult.from(slack);
    }

    public void deleteSlack(UUID sampleId, String deletedBy) {

    }
}
