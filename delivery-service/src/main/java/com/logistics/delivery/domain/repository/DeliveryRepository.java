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

    Optional<Delivery> findByOrderId(UUID orderId);

    Page<Delivery> search(DeliveryStatus status, UUID hubId, Pageable pageable);

    // 본인이 배정된 배송만 (HUB_DELIVERY_MANAGER: route 담당자, COMPANY_DELIVERY_MANAGER: 업체 담당자)
    Page<Delivery> searchByManager(DeliveryStatus status, Long managerId, Pageable pageable);
}
