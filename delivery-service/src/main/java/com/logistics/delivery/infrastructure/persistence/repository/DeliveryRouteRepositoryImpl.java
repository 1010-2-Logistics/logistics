package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryRoute;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DeliveryRouteRepositoryImpl implements DeliveryRouteRepository {

    private final DeliveryRouteJpaRepository jpaRepository;

    @Override
    public DeliveryRoute save(DeliveryRoute deliveryRoute) {
        return jpaRepository.save(deliveryRoute);
    }

    @Override
    public int countByDeliveryId(UUID deliveryId) {
        return jpaRepository.countByDeliveryIdAndDeletedAtIsNull(deliveryId);
    }

    @Override
    public Optional<DeliveryRoute> findByIdAndDeletedAtIsNull(UUID deliveryRouteId) {
        return jpaRepository.findByDeliveryRouteIdAndDeletedAtIsNull(deliveryRouteId);
    }
}