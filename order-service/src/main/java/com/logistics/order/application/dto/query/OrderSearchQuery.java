package com.logistics.order.application.dto.query;

import java.util.UUID;

public record OrderSearchQuery(
        UUID productId,
        UUID endCompanyId,
        String sort,
        Integer page,
        Integer size
) {
}
