package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryJpaRepository jpaRepository;

    @Override
    public Delivery save(Delivery delivery) {
        return jpaRepository.save(delivery);
    }

    @Override
    public Optional<Delivery> findByIdAndDeletedAtIsNull(UUID deliveryId) {
        return jpaRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId);
    }

    @Override
    public Page<Delivery> search(DeliveryStatus status, UUID hubId, Pageable pageable) {
        return jpaRepository.search(status, hubId, pageable);
    }
}
