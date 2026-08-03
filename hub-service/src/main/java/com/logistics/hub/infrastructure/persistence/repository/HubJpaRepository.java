package com.logistics.hub.infrastructure.persistence.repository;

import com.logistics.hub.domain.entity.Hub;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface HubJpaRepository extends JpaRepository<Hub, UUID> {

    Optional<Hub> findByHubIdAndDeletedAtIsNull(UUID hubId);

    @Query("SELECT s FROM Hub s WHERE s.deletedAt IS NULL "
            + "AND (:keyword IS NULL OR s.hubName LIKE %:keyword%)")
    Page<Hub> search(@Param("keyword") String keyword, Pageable pageable);
}
