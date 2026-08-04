package com.logistics.user.presentation.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(@NotBlank String name) {
}
