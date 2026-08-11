package com.logistics.slack.application.event;

import java.util.UUID;

public record AiSlackSendEvent(
        Long receiverId,
        String message,
        UUID referenceId
) {
}
