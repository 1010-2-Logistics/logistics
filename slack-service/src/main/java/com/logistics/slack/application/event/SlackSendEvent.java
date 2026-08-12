package com.logistics.slack.application.event;

import java.util.UUID;

public record SlackSendEvent(
        UUID slackMessageId,
        String receiverSlackId
) {
}
