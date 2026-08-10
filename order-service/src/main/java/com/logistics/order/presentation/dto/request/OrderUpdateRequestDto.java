package com.logistics.order.presentation.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderUpdateRequestDto(
        @Positive(message = "주문 수량은 1 이상이어야 합니다.")
        Integer quantity,

        @Size(max = 500, message = "납품 요청사항은 500자 이하여야 합니다.")
        String request
) {
}
