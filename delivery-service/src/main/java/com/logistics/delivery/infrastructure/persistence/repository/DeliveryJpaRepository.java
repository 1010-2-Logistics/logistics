package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.Delivery;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DeliveryJpaRepository extends JpaRepository<Delivery, UUID> {

}