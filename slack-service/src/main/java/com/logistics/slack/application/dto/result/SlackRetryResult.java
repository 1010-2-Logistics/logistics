package com.logistics.slack.application.dto.result;

import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.entity.SlackStatus;

import java.util.UUID;

public record SlackRetryResult(
        UUID slackMessageId,
        SlackStatus status,
        Integer retryCount,
        String errorMessage
) {
    public static SlackRetryResult from(Slack slack) {
        return new SlackRetryResult(
                slack.getSlackMessageId(),
                slack.getStatus(),
                slack.getRetryCount(),
                slack.getErrorMessage()
        );
    }
}
