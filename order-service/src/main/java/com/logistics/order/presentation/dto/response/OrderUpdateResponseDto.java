package com.logistics.order.presentation.dto.response;

import com.logistics.order.application.dto.result.OrderUpdateResult;

import java.util.UUID;

public record OrderUpdateResponseDto(
        UUID orderId
) {
    public static OrderUpdateResponseDto from(
            OrderUpdateResult orderUpdateResult
    ) {
        return new OrderUpdateResponseDto(
                orderUpdateResult.orderId()
        );
    }
}
