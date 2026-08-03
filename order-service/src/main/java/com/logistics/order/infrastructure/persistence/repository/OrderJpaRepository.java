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

    Optional<Order> findBySampleIdAndDeletedAtIsNull(UUID sampleId);

    @Query("""
        SELECT s
        FROM Order s
        WHERE s.deletedAt IS NULL
          AND (:keyword IS NULL OR s.name LIKE %:keyword%)
        """)
    Page<Order> search(@Param("keyword") String keyword, Pageable pageable);
}
