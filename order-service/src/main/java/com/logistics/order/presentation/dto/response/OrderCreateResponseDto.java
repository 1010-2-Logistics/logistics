package com.logistics.order.presentation.dto.response;

import com.logistics.order.application.dto.result.OrderCreateResult;

import java.util.UUID;

public record OrderCreateResponseDto(
        UUID orderId
) {
    public static OrderCreateResponseDto from(
            OrderCreateResult orderCreateResult
    ) {
        return new OrderCreateResponseDto(
                orderCreateResult.orderId()
        );
    }
}
