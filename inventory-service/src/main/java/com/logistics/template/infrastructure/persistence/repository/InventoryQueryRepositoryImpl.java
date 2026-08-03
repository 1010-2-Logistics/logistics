package com.logistics.template.infrastructure.persistence.repository;

import com.logistics.template.domain.entity.Inventory;
import com.logistics.template.domain.repository.InventoryQueryRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryQueryRepositoryImpl implements InventoryQueryRepository {

    private final InventoryJpaRepository jpaRepository;

    @Override
    public Optional<Inventory> findByIdAndDeletedAtIsNull(UUID sampleId) {
        return jpaRepository.findByInventoryIdAndDeletedAtIsNull(sampleId);
    }

    @Override
    public Page<Inventory> search(String keyword, Pageable pageable) {
        return jpaRepository.search(keyword, pageable);
    }
}
