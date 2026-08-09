package com.logistics.slack.presentation.dto.response;

import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.domain.entity.SlackStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackCreateResponseDto(
        UUID slackMessageId,
        Long senderId,
        Long receiverId,
        String message,
        SlackStatus status,
        Integer retryCount,
        UUID referenceId,
        LocalDateTime createdAt
) {
    public static SlackCreateResponseDto from(
            SlackCreateResult slackCreatResult
    ) {
        return new SlackCreateResponseDto(
                slackCreatResult.slackMessageId(),
                slackCreatResult.senderId(),
                slackCreatResult.receiverId(),
                slackCreatResult.message(),
                slackCreatResult.status(),
                slackCreatResult.retryCount(),
                slackCreatResult.referenceId(),
                slackCreatResult.createdAt()
        );
    }
}
