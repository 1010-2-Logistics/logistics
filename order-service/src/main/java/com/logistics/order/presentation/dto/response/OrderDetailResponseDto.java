package com.logistics.order.presentation.dto.response;

import com.logistics.order.application.dto.result.OrderDetailResult;
import com.logistics.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDetailResponseDto(
        UUID orderId,
        UUID startCompanyId,
        UUID endCompanyId,
        UUID productId,
        UUID deliveryId,
        Integer quantity,
        OrderStatus status,
        String request,
        LocalDateTime createdAt
) {
    public static OrderDetailResponseDto from(OrderDetailResult orderDetailResult) {
        return new OrderDetailResponseDto(
                orderDetailResult.orderId(),
                orderDetailResult.startCompanyId(),
                orderDetailResult.endCompanyId(),
                orderDetailResult.productId(),
                orderDetailResult.deliveryId(),
                orderDetailResult.quantity(),
                orderDetailResult.status(),
                orderDetailResult.request(),
                orderDetailResult.createdAt()
        );
    }
}
