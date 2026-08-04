package com.logistics.order.application.dto.result;

import com.logistics.order.domain.entity.Order;

import java.util.UUID;

public record OrderCreateResult(
        UUID orderId
) {
    public static OrderCreateResult from(Order order) {
        return new OrderCreateResult(
                order.getOrderId()
        );
    }
}
