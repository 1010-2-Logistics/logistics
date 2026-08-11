package com.logistics.slack.infrastructure.feign.response;

public record UserInfoResponse(
        Long userId,
        String name,
        String slackId
) {
}
