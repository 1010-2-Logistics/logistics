package com.logistics.hubRoute.infrastructure.persistence.repository;

import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.domain.repository.HubRouteQueryRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HubRouteQueryRepositoryImpl implements HubRouteQueryRepository {

    private final HubRouteJpaRepository jpaRepository;

    @Override
    public Optional<HubRoute> findByIdAndDeletedAtIsNull(UUID hubRouteId) {
        return jpaRepository.findByHubRouteIdAndDeletedAtIsNull(hubRouteId);
    }

    @Override
    public Page<HubRoute> search(UUID hubId, Pageable pageable) {
        return jpaRepository.search(hubId, pageable);
    }
}
