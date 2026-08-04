package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryManagerRepositoryImpl implements DeliveryManagerRepository {

    private final DeliveryManagerJpaRepository jpaRepository;

    @Override
    public DeliveryManager save(DeliveryManager manager) {
        return jpaRepository.save(manager);
    }

    @Override
    public boolean existsByDeliveryManagerIdAndDeletedAtIsNull(Long deliveryManagerId) {
        return jpaRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(deliveryManagerId);
    }

    @Override
    public Optional<Integer> findMaxSequence(ManagerType managerType, UUID hubId) {
        return jpaRepository.findMaxSequence(managerType, hubId);
    }
}