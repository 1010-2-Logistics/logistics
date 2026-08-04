package com.logistics.order.infrastructure.persistence.repository;

import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.repository.OrderCommandRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderCommandRepositoryImpl implements OrderCommandRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public Order save(Order sample) {
        return jpaRepository.save(sample);
    }

    @Override
    public Optional<Order> findByIdAndDeletedAtIsNull(UUID sampleId) {
        return jpaRepository.findByOrderIdAndDeletedAtIsNull(sampleId);
    }
}
