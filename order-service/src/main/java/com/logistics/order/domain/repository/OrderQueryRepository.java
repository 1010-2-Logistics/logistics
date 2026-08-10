package com.logistics.order.domain.repository;

import com.logistics.order.application.dto.query.OrderReadScope;
import com.logistics.order.domain.entity.Order;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryRepository {

    Optional<Order> findByIdAndDeletedAtIsNull(UUID orderId);

    Page<Order> search(
            UUID productId,
            UUID endCompanyId,
            OrderReadScope readScope,
            Pageable pageable
    );
}
