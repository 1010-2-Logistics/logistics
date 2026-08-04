package com.logistics.inventory.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record InventoryCreateRequestDto(
        @NotNull
        UUID productId,

        @NotNull
        UUID hubId,

        @NotNull
        @PositiveOrZero // 숫자가 0 이상인지 검사하는 Jakarta Validation 애너테이션
        Integer stock
) {
}
