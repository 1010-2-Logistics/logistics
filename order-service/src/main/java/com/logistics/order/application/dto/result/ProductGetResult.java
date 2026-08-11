package com.logistics.order.application.dto.result;

import java.util.UUID;

public record ProductGetResult(
        UUID productId,
        UUID companyId,
        String productName
) {
}
