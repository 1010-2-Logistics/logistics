package com.logistics.user.application.dto.command;

/**
 * 로그인한 사용자의 Slack ID 변경에 필요한 입력값.
 */
public record UpdateMySlackIdCommandDto(
        Long userId,
        String slackId
) {
}