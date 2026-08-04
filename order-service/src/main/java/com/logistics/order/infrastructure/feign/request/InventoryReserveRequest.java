package com.logistics.order.infrastructure.feign.request;

import java.util.UUID;

public record InventoryReserveRequest(
        UUID productId,
        UUID hubId,
        Integer stock
) {
}
