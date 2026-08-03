package com.logistics.template.presentation.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SampleUpdateRequest(@NotBlank String name) {
}
