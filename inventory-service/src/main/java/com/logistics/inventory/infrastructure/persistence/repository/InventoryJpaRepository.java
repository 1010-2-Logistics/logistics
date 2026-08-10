package com.logistics.inventory.infrastructure.persistence.repository;

import com.logistics.inventory.domain.entity.Inventory;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface InventoryJpaRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByInventoryIdAndDeletedAtIsNull(UUID inventoryId);

    Optional<Inventory> findByProductIdAndHubIdAndDeletedAtIsNull(
            UUID productId,
            UUID hubId
    );

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.deletedAt IS NULL
              AND (:productId IS NULL OR i.productId = :productId)
              AND (:hubId IS NULL OR i.hubId = :hubId)
            """)
    Page<Inventory> search(
            @Param("productId") UUID productId,
            @Param("hubId") UUID hubId,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.productId = :productId
              AND i.hubId = :hubId
              AND i.deletedAt IS NULL
            """)
    Optional<Inventory> findByProductAndHubIdWithLock(
            @Param("productId") UUID productId,
            @Param("hubId") UUID hubId
    );
}
