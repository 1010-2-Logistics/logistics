package com.logistics.hubRoute.domain.repository;

import com.logistics.hubRoute.domain.entity.HubRoute;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRouteQueryRepository {

    Optional<HubRoute> findByIdAndDeletedAtIsNull(UUID hubRouteId);

    Page<HubRoute> search(UUID hubRouteId, Pageable pageable);
}
