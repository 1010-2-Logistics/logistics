package com.logistics.inventory.domain.repository;

import com.logistics.inventory.domain.entity.Inventory;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryQueryRepository {
    Page<Inventory> search(
            UUID productId,
            UUID hubId,
            Pageable pageable
    );

    Optional<Inventory> findByInventoryIdAndDeletedAtIsNull(
            UUID inventoryId
    );
}
