package com.logistics.hubRoute.infrastructure.persistence.repository;

import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.domain.repository.HubRouteCommandRepository;

import java.math.BigDecimal;
import java.util.List;
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

    //동일한 경로가 존재하는지 확인
    @Override
    public boolean existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(UUID startHubId, UUID endHubId) {
        return jpaRepository.existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(startHubId, endHubId);
    }

    @Override
    public boolean existsByStartHubIdAndEndHubIdAndHubRouteIdNotAndDeletedAtIsNull(UUID startHubId, UUID endHubId, UUID hubRouteId) {
        return jpaRepository.existsByStartHubIdAndEndHubIdAndHubRouteIdNotAndDeletedAtIsNull(startHubId, endHubId, hubRouteId);
    }

    @Override
    public boolean findByHubRouteIdAndDeletedAtIsNull(UUID hubRouteId) {
        return jpaRepository.existsByHubRouteIdAndDeletedAtIsNull(hubRouteId);
    }

    //허브 삭제시 허브 경로도 삭제
    @Override
    public List<HubRoute> findAllByStartHubIdOrEndHubIdAndDeletedAtIsNull(UUID starHubId, UUID endHubId) {
        return jpaRepository.findAllByStartHubIdOrEndHubIdAndDeletedAtIsNull(starHubId, endHubId);
    }

}