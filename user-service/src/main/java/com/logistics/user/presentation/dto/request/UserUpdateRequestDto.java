package com.logistics.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDto(@NotBlank String slackId) {
}