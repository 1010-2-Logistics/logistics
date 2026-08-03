package com.logistics.inventory.domain.repository;

import com.logistics.inventory.domain.entity.Inventory;
import java.util.Optional;
import java.util.UUID;

public interface InventoryCommandRepository {
    Inventory save(Inventory inventory);

    Optional<Inventory> findByIdAndDeletedAtIsNull(UUID inventoryId);
}
