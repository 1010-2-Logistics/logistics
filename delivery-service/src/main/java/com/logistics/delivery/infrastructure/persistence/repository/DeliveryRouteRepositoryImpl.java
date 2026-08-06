package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryRoute;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryRouteRepositoryImpl implements DeliveryRouteRepository {

    private final DeliveryRouteJpaRepository jpaRepository;

    @Override
    public DeliveryRoute save(DeliveryRoute deliveryRoute) {
        return jpaRepository.save(deliveryRoute);
    }
}