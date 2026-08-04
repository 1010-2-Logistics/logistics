package com.logistics.hubRoute.infrastructure.persistence.repository;

import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.domain.repository.HubRouteCommandRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HubRouteCommandRepositoryImpl implements HubRouteCommandRepository {

    private final HubRouteJpaRepository jpaRepository;

    @Override
    public HubRoute save(HubRoute hub) {
        return jpaRepository.save(hub);
    }

    @Override
    public Optional<HubRoute> findByIdAndDeletedAtIsNull(UUID hubRouteId) {
        return jpaRepository.findByHubRouteIdAndDeletedAtIsNull(hubRouteId);
    }


}
