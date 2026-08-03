package com.logistics.inventory.infrastructure.persistence.repository;

import com.logistics.inventory.domain.entity.Inventory;
import com.logistics.inventory.domain.repository.InventoryQueryRepository;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryQueryRepositoryImpl implements InventoryQueryRepository {
    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public Page<Inventory> search(
            UUID productId,
            UUID hubId,
            Pageable pageable
    ) {
        return inventoryJpaRepository.search(
                productId,
                hubId,
                pageable
        );
    }
}
