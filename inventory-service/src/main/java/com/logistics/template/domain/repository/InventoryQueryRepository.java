package com.logistics.template.domain.repository;

import com.logistics.template.domain.entity.Inventory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryQueryRepository {

    Optional<Inventory> findByIdAndDeletedAtIsNull(UUID sampleId);

    Page<Inventory> search(String keyword, Pageable pageable);
}
