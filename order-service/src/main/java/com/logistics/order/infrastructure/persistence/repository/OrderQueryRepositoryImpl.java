package com.logistics.order.infrastructure.persistence.repository;

import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.repository.OrderQueryRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final OrderJpaRepository jpaRepository;


    @Override
    public Optional<Order> findByIdAndDeletedAtIsNull(UUID sampleId) {
        return Optional.empty();
    }

    @Override
    public Page<Order> search(String keyword, Pageable pageable) {
        return null;
    }
}
