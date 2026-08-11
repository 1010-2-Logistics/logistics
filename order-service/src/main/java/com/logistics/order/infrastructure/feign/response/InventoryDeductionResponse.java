package com.logistics.order.infrastructure.feign.response;

import java.util.UUID;

public record InventoryDeductionResponse(
        UUID operationId,
        UUID orderId,
        UUID productId,
        UUID hubId,
        Integer quantity
) {
}
