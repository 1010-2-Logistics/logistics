package com.logistics.hub.infrastructure.persistence.repository;

import com.logistics.hub.domain.entity.Hub;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface HubJpaRepository extends JpaRepository<Hub, UUID> {

    Optional<Hub> findByHubIdAndDeletedAtIsNull(UUID hubId);

    @Query("SELECT h FROM Hub h WHERE h.deletedAt IS NULL "
            + "AND (:hubId IS NULL OR h.hubId = :hubId)")
    Page<Hub> search(@Param("hubId") UUID hubId, Pageable pageable);

    // 1. 위도 + 경도 중복 체크
    boolean existsByLatitudeAndLongitudeAndDeletedAtIsNull(BigDecimal latitude, BigDecimal longitude);

    // 2. 주소 중복 체크
    boolean existsByHubAddressAndDeletedAtIsNull(String hubAddress);

    // 3. 위도 + 경도 중복 체크 (자기 자신 제외)
    boolean existsByLatitudeAndLongitudeAndHubIdNotAndDeletedAtIsNull(BigDecimal latitude, BigDecimal longitude, UUID hubId);

    // 4. 주소 중복 체크 (자기 자신 제외)
    boolean existsByHubAddressAndHubIdNotAndDeletedAtIsNull(String hubAddress, UUID hubId);

    //허브 존재 여부 체크
    boolean existsByhubIdAndDeletedAtIsNull(UUID hubId);
}
