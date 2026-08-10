package com.logistics.order.application.dto.result;

import com.logistics.order.domain.entity.Order;

import java.util.UUID;

public record OrderUpdateResult(
        UUID orderId
) {
    public static OrderUpdateResult from(Order order) {
        return new OrderUpdateResult(order.getOrderId());
    }
}
