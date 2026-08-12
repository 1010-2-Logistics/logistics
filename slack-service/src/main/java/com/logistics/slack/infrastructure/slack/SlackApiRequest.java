package com.logistics.slack.infrastructure.slack;

public record SlackApiRequest(
        String channel,
        String text
) {
}
