package com.logistics.slack.application.event;

import java.util.UUID;

public record SlackSendEvent(
        UUID slackMessageId,
        // TODO AI 연동 후 receiverSlackId 추가 예정
        String receiverSlackId
) {
}
