package com.logistics.hub.presentation.dto.dto.request;

import jakarta.validation.constraints.NotBlank;

public record HubUpdateRequest(@NotBlank String name) {
}
