package com.logistics.order.application.dto.query;

import java.util.UUID;

public record OrderReadScope(
        boolean all,
        UUID hubId,
        UUID companyId,
        Long deliveryManagerId
) {
    public static OrderReadScope allOrders() {
        return new OrderReadScope(true, null, null, null);
    }

    public static OrderReadScope company(UUID companyId) {
        return new OrderReadScope(false, null, companyId, null);
    }

    public static OrderReadScope hub(UUID hubId) {
        return new OrderReadScope(false, hubId, null, null);
    }

    public static OrderReadScope deliveryManager(Long userId) {
        return new OrderReadScope(false, null, null, userId);
    }
}