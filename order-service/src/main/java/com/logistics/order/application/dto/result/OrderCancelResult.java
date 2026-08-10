package com.logistics.order.application.dto.result;

import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.entity.OrderStatus;

import java.util.UUID;

public record OrderCancelResult(
        UUID orderId,
        OrderStatus status
) {
    public static OrderCancelResult from(Order order) {
        return new OrderCancelResult(
                order.getOrderId(),
                order.getStatus()
        );
    }
}
