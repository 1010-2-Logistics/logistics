package com.logistics.slack.presentation.dto.request;

import java.util.UUID;

public record SlackCreateRequestDto(
        String receiverId,
        String message,
        UUID referenceId
) {
}
