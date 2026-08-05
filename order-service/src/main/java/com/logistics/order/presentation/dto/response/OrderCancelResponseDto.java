package com.logistics.order.presentation.dto.response;

import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.domain.entity.OrderStatus;

import java.util.UUID;

public record OrderCancelResponseDto(
        UUID orderId,
        OrderStatus status
) {
    public static OrderCancelResponseDto from(
            OrderCancelResult orderCancelResult
    ) {
        return new OrderCancelResponseDto(
                orderCancelResult.orderId(),
                orderCancelResult.status()
        );
    }
}
