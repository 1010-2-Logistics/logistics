package com.logistics.slack.infrastructure.slack;

public record SlackApiResponse(
        boolean ok,
        String error
) {
}
