package com.logistics.hub.presentation.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record HubUpdateRequest(@NotBlank String name) {
}
