package com.logistics.inventory.application.dto.query;


import java.util.UUID;

public record SearchInventoryQuery(
        UUID productId,
        UUID hubId,
        String sort,
        Integer page,
        Integer size
) {
}
