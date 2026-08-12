package com.logistics.slack.application.dto.result;

public record UserInfo(
        Long userId,
        String name,
        String slackId
) {
}
