package com.logistics.slack.presentation.dto.response;

import com.logistics.slack.application.dto.result.SlackRetryResult;
import com.logistics.slack.domain.entity.SlackStatus;

import java.util.UUID;

public record SlackRetryResponseDto(
        UUID slackMessageId,
        SlackStatus status,
        Integer retryCount,
        String errorMessage
) {
    public static SlackRetryResponseDto from(
            SlackRetryResult slackRetryResult
    ) {
        return new SlackRetryResponseDto(
                slackRetryResult.slackMessageId(),
                slackRetryResult.status(),
                slackRetryResult.retryCount(),
                slackRetryResult.errorMessage()
        );
    }
}
