package com.logistics.template.infrastructure.persistence.repository;

import com.logistics.template.domain.entity.Inventory;
import com.logistics.template.domain.repository.InventoryCommandRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryCommandRepositoryImpl implements InventoryCommandRepository {

    private final InventoryJpaRepository jpaRepository;

    @Override
    public Inventory save(Inventory sample) {
        return jpaRepository.save(sample);
    }

    @Override
    public Optional<Inventory> findByIdAndDeletedAtIsNull(UUID sampleId) {
        return jpaRepository.findByInventoryIdAndDeletedAtIsNull(sampleId);
    }
}
