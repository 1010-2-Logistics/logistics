package com.logistics.slack.application.service;

import com.logistics.slack.application.authorization.SlackAuthorizationService;
import com.logistics.slack.application.dto.auth.AuthenticatedUser;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class SlackCommandService {
    private static final int MAX_RETRY_COUNT = 3;
    private final SlackCommandRepository slackCommandRepository;
    private final SlackEventPublisher slackEventPublisher;
    private final SlackMessageSender slackMessageSender;
    private final SlackAuthorizationService slackAuthorizationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SlackCreateResult createSlack(
            SlackCreateCommand slackCreateCommand,
            AuthenticatedUser authenticatedUser
    ) {
        Slack slack = Slack.create(
                authenticatedUser.userId(),
                slackCreateCommand.receiverId(),
                slackCreateCommand.message(),
                slackCreateCommand.referenceId()
        );
        slackCommandRepository.save(slack);

        applicationEventPublisher.publishEvent(
                new SlackSendEvent(slack.getSlackMessageId())
        );

        // 이 시점의 slack.status는 아직 PENDING!
        return SlackCreateResult.from(slack);
    }

    public void send(
            UUID slackMessageId,
            String receiverSlackId
    ) {
        Slack slack = slackCommandRepository
                .findByIdAndDeletedAtIsNull(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_NOT_FOUND));

        try {
            slackMessageSender.send(
                    receiverSlackId,
                    slack.getMessage()
            );
            slack.markSuccess();

        } catch (Exception e) {
            slack.markFailed(e.getMessage());
        }
    }

    public SlackRetryResult retrySlack(
            UUID slackMessageId,
            AuthenticatedUser authenticatedUser
    ) {
        Slack slack = slackCommandRepository
                .findByIdAndDeletedAtIsNull(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_NOT_FOUND));

        slackAuthorizationService.validateAccess(
                authenticatedUser,
                slack
        );

        slack.retry(MAX_RETRY_COUNT);

        applicationEventPublisher.publishEvent(
                new SlackSendEvent(slack.getSlackMessageId())
        );

        return SlackRetryResult.from(slack);
    }

    public void deleteSlack(
            UUID slackMessageId,
            AuthenticatedUser authenticatedUser
    ) {
        Slack slack = slackCommandRepository
                .findByIdAndDeletedAtIsNull(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_NOT_FOUND));

        slackAuthorizationService.validateAccess(
                authenticatedUser
        );

        slack.markDeleted(authenticatedUser.userId());
    }
}
