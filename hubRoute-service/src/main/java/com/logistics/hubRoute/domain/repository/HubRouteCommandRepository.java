package com.logistics.hubRoute.domain.repository;

import com.logistics.hubRoute.domain.entity.HubRoute;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface HubRouteCommandRepository {

    HubRoute save(HubRoute hub);

    Optional<HubRoute> findByIdAndDeletedAtIsNull(UUID hubId);


}
