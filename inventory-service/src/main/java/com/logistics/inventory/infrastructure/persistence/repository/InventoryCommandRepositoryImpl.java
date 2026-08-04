package com.logistics.inventory.infrastructure.persistence.repository;

import com.logistics.inventory.domain.entity.Inventory;
import com.logistics.inventory.domain.repository.InventoryCommandRepository;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryCommandRepositoryImpl implements InventoryCommandRepository {
    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public Optional<Inventory> findByProductAndHubId(
            UUID productId,
            UUID hubId
    ) {
        return inventoryJpaRepository.findByProductIdAndHubIdAndDeletedAtIsNull(
                productId,
                hubId
        );
    }

    @Override
    public Optional<Inventory> findByProductIdAndHubIdAndDeletedAtIsNull(UUID productId, UUID hubId) {
        return inventoryJpaRepository.findByProductIdAndHubIdAndDeletedAtIsNull(
                productId,
                hubId
        );
    }

    @Override
    public Inventory save(Inventory inventory) {
        return inventoryJpaRepository.save(inventory);
    }

    @Override
    public Optional<Inventory> findByIdAndDeletedAtIsNull(UUID inventoryId) {
        return inventoryJpaRepository.findByInventoryIdAndDeletedAtIsNull(inventoryId);
    }
}
