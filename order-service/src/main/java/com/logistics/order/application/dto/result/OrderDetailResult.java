package com.logistics.order.application.dto.result;

import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDetailResult(
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
    public static OrderDetailResult from(Order order) {
        return new OrderDetailResult(
                order.getOrderId(),
                order.getStartCompanyId(),
                order.getEndCompanyId(),
                order.getProductId(),
                order.getDeliveryId(),
                order.getQuantity(),
                order.getStatus(),
                order.getRequest(),
                order.getCreatedAt()
        );
    }
}
