package com.logistics.hub.infrastructure.persistence.repository;

import com.logistics.hub.domain.entity.Hub;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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


    //허브아이디가 다르면서, 허브 위도 경도가 같거나, 허브 주소가 같은 경우 체크
    @Query("SELECT COUNT(h) > 0 FROM Hub h " +
            "WHERE h.hubId <> :hubId AND h.deletedAt IS NULL " +
            "AND ((h.latitude = :latitude AND h.longitude = :longitude) OR h.hubAddress = :hubAddress)")
    boolean existsDuplicateHubForUpdate(@Param("hubId") UUID hubId,
                                        @Param("latitude") BigDecimal latitude,
                                        @Param("longitude") BigDecimal longitude,
                                        @Param("hubAddress") String hubAddress);

    @Query("SELECT h.hubId FROM Hub h WHERE h.hubId IN :hubIds AND h.deletedAt IS NULL")
    Set<UUID> findValidHubIdsIn(@Param("hubIds") List<UUID> hubIds);

}
