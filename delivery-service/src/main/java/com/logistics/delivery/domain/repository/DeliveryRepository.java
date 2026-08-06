package com.logistics.delivery.domain.repository;

import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryRepository {

    Delivery save(Delivery delivery);

    Optional<Delivery> findByIdAndDeletedAtIsNull(UUID deliveryId);

    Page<Delivery> search(DeliveryStatus status, UUID hubId, Pageable pageable);
}
