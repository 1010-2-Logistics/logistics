package com.logistics.order.presentation.dto.response;

import com.logistics.order.application.dto.result.OrderListItemResult;

import java.util.UUID;

public record OrderListItemResponseDto(
        UUID orderId,
        UUID startCompanyId,
        UUID endCompanyId,
        UUID productId,
        UUID deliveryId,
        Integer quantity,
        String request
) {
    public static OrderListItemResponseDto from(OrderListItemResult orderListItemResult) {
        return new OrderListItemResponseDto(
                orderListItemResult.orderId(),
                orderListItemResult.startCompanyId(),
                orderListItemResult.endCompanyId(),
                orderListItemResult.productId(),
                orderListItemResult.deliveryId(),
                orderListItemResult.quantity(),
                orderListItemResult.request()
        );
    }
}
