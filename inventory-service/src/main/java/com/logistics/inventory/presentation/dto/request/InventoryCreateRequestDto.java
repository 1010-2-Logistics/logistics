package com.logistics.inventory.presentation.dto.request;

import java.util.UUID;

public record InventoryCreateRequestDto(
        UUID productId,
        UUID hubId,
        Integer stock
) {
}
