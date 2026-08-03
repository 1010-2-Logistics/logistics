package com.logistics.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderUpdateRequest(@NotBlank String name) {
}
