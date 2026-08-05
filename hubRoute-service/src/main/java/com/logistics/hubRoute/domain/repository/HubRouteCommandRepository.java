package com.logistics.hubRoute.domain.repository;

import com.logistics.hubRoute.domain.entity.HubRoute;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface HubRouteCommandRepository {

    HubRoute save(HubRoute hub);

    Optional<HubRoute> findByIdAndDeletedAtIsNull(UUID hubRouteId);

    //동일한 경로가 이미 존재하는지 확인
    boolean existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(UUID startHubId, UUID endHubId);

    //허브 수정 동일한 경로인지 탐색[자기 자신은 제외]
    boolean existsByStartHubIdAndEndHubIdAndHubRouteIdNotAndDeletedAtIsNull(UUID startHubId, UUID endHubId, UUID hubRouteId);
}
