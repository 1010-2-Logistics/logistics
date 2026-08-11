package com.logistics.order.application.dto.result;

public record UserInfoResult(
        Long userId,
        String name,
        String slackId
) {
}
