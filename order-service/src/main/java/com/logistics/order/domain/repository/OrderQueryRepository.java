package com.logistics.order.domain.repository;

import com.logistics.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderQueryRepository {
    Optional<Order> findByIdAndDeletedAtIsNull(UUID orderId);

    Page<Order> search(
            UUID productId,
            UUID endCompanyId,
            Pageable pageable
    );

    Page<Order> searchByCreatedBy(
            Long createdBy,
            UUID productId,
            UUID endCompanyId,
            Pageable pageable
    );
}
