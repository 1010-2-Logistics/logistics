package com.logistics.order.presentation.dto.response;

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
    public static OrderListItemResponseDto from(OrderListItemResponseDto orderListItemResponseDto) {
        return new OrderListItemResponseDto(
                orderListItemResponseDto.orderId(),
                orderListItemResponseDto.startCompanyId(),
                orderListItemResponseDto.endCompanyId(),
                orderListItemResponseDto.productId(),
                orderListItemResponseDto.deliveryId(),
                orderListItemResponseDto.quantity(),
                orderListItemResponseDto.request()
        );
    }
}
