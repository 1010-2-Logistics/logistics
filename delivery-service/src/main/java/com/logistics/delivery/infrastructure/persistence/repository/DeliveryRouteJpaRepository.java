package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryRoute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

interface DeliveryRouteJpaRepository extends JpaRepository<DeliveryRoute, UUID> {
    int countByDeliveryIdAndDeletedAtIsNull(UUID deliveryId);
    Optional<DeliveryRoute> findByDeliveryRouteIdAndDeletedAtIsNull(UUID deliveryRouteId);
    List<DeliveryRoute> findAllByDeliveryIdAndDeletedAtIsNullOrderBySequenceAsc(UUID deliveryId);

    // "ForUpdate"는 Spring Data가 아는 키워드가 아니라 이름만으로는 파싱이 깨진다(#245).
    // @Query를 쓰면 이름이 파싱 대상이 아니게 되고, 락은 @Lock이 별도로 적용한다.
    // @Transactional 없으면 기본 readOnly 트랜잭션이 씌워져 Postgres가 FOR UPDATE를 거부한다.
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM DeliveryRoute r WHERE r.deliveryRouteId = :deliveryRouteId AND r.deletedAt IS NULL")
    Optional<DeliveryRoute> findByDeliveryRouteIdAndDeletedAtIsNullForUpdate(@Param("deliveryRouteId") UUID deliveryRouteId);
}