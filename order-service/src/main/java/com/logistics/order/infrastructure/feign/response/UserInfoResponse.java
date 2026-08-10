package com.logistics.order.infrastructure.feign.response;

public record UserInfoResponse(
        Long userId,
        String name,
        String slackId
) {
}
