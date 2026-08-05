package com.logistics.slack.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SlackCreateRequestDto(
        @NotNull
        UUID receiverId,

        @NotBlank
        String message,

        UUID referenceId
) {
}
