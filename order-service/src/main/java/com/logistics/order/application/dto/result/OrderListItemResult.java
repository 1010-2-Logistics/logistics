package com.logistics.order.application.dto.result;

import com.logistics.order.domain.entity.Order;

import java.util.UUID;

public record OrderListItemResult(
        UUID orderId,
        UUID startCompanyId,
        UUID endCompanyId,
        UUID productId,
        UUID deliveryId,
        Integer quantity,
        String request
) {
    public static OrderListItemResult from(Order order){
        return new OrderListItemResult(
                order.getOrderId(),
                order.getStartCompanyId(),
                order.getEndCompanyId(),
                order.getDeliveryId(),
                order.getQuantity(),
                order.getRequest()
        );
    }
}
