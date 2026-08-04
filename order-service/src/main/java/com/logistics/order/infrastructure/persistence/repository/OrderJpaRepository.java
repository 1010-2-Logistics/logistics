package com.logistics.order.infrastructure.persistence.repository;

import com.logistics.order.domain.entity.Order;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    @Query("""
            SELECT o
            FROM Order o
            WHERE o.deletedAt IS NULL
              AND (:productId IS NULL OR o.productId = :productId)
              AND (:endCompanyId IS NULL OR o.endCompanyId = :endCompanyId)
            """)
    Page<Order> search(
            @Param("productId") UUID productId,
            @Param("endCompanyId") UUID endCompanyId,
            Pageable pageable
    );
}
