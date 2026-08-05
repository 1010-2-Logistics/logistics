package com.logistics.slack.presentation.dto.response;

import com.logistics.slack.application.dto.result.SlackCreatResult;
import com.logistics.slack.domain.entity.SlackStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackCreateResponseDto(
        UUID slackMessageId,
        String senderId,
        String receiverId,
        String message,
        SlackStatus status,
        Integer retryCount,
        UUID referenceId,
        LocalDateTime createdAt
) {
    public static SlackCreateResponseDto from(
            SlackCreatResult slackCreatResult
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
