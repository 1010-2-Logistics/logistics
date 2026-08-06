package com.logistics.slack.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SlackCreateRequestDto(
        @NotNull
        Long receiverId,

        @NotBlank
        String message,

        Long referenceId
) {
}
