package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.Delivery;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryJpaRepository jpaRepository;

    @Override
    public Delivery save(Delivery delivery) {
        return jpaRepository.save(delivery);
    }
}