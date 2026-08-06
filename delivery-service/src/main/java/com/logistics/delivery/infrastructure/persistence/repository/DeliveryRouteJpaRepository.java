package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryRoute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DeliveryRouteJpaRepository extends JpaRepository<DeliveryRoute, UUID> {
}