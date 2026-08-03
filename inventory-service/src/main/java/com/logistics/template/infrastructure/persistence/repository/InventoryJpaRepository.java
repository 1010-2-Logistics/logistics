package com.logistics.template.infrastructure.persistence.repository;

import com.logistics.template.domain.entity.Inventory;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface InventoryJpaRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByInventoryIdAndDeletedAtIsNull(UUID sampleId);

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.deletedAt IS NULL
            AND (:keyword IS NULL OR i.name LIKE %:keyword%)
            """)
    Page<Inventory> search(@Param("keyword") String keyword, Pageable pageable);
}
