package com.logistics.slack.presentation.dto.response;

import com.logistics.slack.application.dto.result.SlackListResult;
import com.logistics.slack.domain.entity.SlackStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackListResponseDto(
        UUID slackMessageId,
        Long senderId,
        Long receiverId,
        String message,
        SlackStatus status,
        Integer retryCount,
        LocalDateTime sentAt,
        Long referenceId,
        LocalDateTime createdAt
) {
    public static SlackListResponseDto from(
            SlackListResult slackSummaryResult
    ) {
        return new SlackListResponseDto(
                slackSummaryResult.slackMessageId(),
                slackSummaryResult.senderId(),
                slackSummaryResult.receiverId(),
                slackSummaryResult.message(),
                slackSummaryResult.status(),
                slackSummaryResult.retryCount(),
                slackSummaryResult.sentAt(),
                slackSummaryResult.referenceId(),
                slackSummaryResult.createdAt()
        );
    }
}
