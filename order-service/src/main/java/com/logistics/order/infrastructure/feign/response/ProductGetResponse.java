package com.logistics.order.infrastructure.feign.response;

import java.util.UUID;

public record ProductGetResponse(
        UUID productId,
        UUID companyId,
        String productName
) {
}
