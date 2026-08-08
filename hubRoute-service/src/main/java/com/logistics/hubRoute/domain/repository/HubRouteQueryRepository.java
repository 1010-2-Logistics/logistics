package com.logistics.hubRoute.domain.repository;

import com.logistics.hubRoute.domain.entity.HubRoute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRouteQueryRepository {

    Optional<HubRoute> findByIdAndDeletedAtIsNull(UUID hubRouteId);

    Page<HubRoute> search(UUID hubRouteId, Pageable pageable);

    //시작허브와 도착허브 아이디 값을 통해 탐색
    Optional<HubRoute> findByStartHubIdAndEndHubIdAndDeletedAtIsNull(UUID startHubId, UUID endHubId);

    //허브 전체 검색 (페이지 없음, 경로 생성 용)
    List<HubRoute> findAllByDeletedAtIsNull();
}
