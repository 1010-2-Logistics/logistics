package com.logistics.hubRoute.infrastructure.persistence.repository;

import com.logistics.hubRoute.domain.entity.HubRoute;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface HubRouteJpaRepository extends JpaRepository<HubRoute, UUID> {

    Optional<HubRoute> findByHubRouteIdAndDeletedAtIsNull(UUID hubRouteId);

    @Query("SELECT h FROM HubRoute h WHERE h.deletedAt IS NULL "
            + "AND (:hubRouteId IS NULL OR h.hubRouteId = :hubRouteId)")
    Page<HubRoute> search(@Param("hubRouteId") UUID hubRouteId, Pageable pageable);

    boolean existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(UUID startHubId, UUID endHubId);
}
