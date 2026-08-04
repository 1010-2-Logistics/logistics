package com.logistics.user.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(@NotBlank String slackId) {
}