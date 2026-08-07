package com.logistics.slack.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SlackCreateRequestDto(
        @NotNull
        Long receiverId,

        @NotBlank
        @Size(max = 5000)
        String message,

        UUID referenceId
) {
}
