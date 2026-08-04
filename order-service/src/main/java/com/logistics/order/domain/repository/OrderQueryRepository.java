package com.logistics.order.domain.repository;

import com.logistics.order.domain.entity.Order;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryRepository {

    Optional<Order> findByIdAndDeletedAtIsNull(UUID orderId);

    Page<Order> search(String keyword, Pageable pageable);
}
