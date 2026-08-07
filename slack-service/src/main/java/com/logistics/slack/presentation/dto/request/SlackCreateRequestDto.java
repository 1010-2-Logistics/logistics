package com.logistics.slack.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SlackCreateRequestDto(
        @NotNull(message = "수신인 ID는 필수입니다.")
        Long receiverId,

        @NotBlank(message = "메시지 내용은 필수입니다.")
        @Size(max = 5000)
        String message,

        Long referenceId
) {
}
