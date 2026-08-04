package com.logistics.order.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OrderCreateRequestDto(
        @NotNull
        UUID startCompanyId,

        @NotNull
        UUID endCompanyId,

        @NotNull
        UUID productId,

        @Positive(message = "주문 수량은 1 이상이어야 합니다.")
        @NotNull
        Integer quantity,

        @NotNull
        String request
) {
}
