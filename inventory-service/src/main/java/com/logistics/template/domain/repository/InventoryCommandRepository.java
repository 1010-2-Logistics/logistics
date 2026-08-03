package com.logistics.template.domain.repository;

import com.logistics.template.domain.entity.Inventory;
import java.util.Optional;
import java.util.UUID;

public interface InventoryCommandRepository {

    Inventory save(Inventory sample);

    Optional<Inventory> findByIdAndDeletedAtIsNull(UUID sampleId);
}
