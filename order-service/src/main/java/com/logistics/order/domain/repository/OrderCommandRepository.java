package com.logistics.order.domain.repository;

import com.logistics.order.domain.entity.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderCommandRepository {

    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByIdAndDeletedAtIsNull(UUID orderId);
}
