package com.logistics.inventory.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record SearchInventoryQuery(
        UUID productId,
        UUID hubId,
        Pageable pageable
) {
}
