package com.logistics.inventory.presentation.dto.request;

import java.util.UUID;

public record InventorySearchRequest(
        UUID productId,
        UUID hubId,
        String sort,
        Integer page,
        Integer size
) {
}
