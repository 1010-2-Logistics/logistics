package com.logistics.delivery.domain.repository;

import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryManagerRepository {

    DeliveryManager save(DeliveryManager manager);

    boolean existsByDeliveryManagerIdAndDeletedAtIsNull(Long deliveryManagerId);

    // 신규 등록 시 "마지막 순번" 조회용 (허브담당자=전체기준, 업체담당자=허브별기준)
    Optional<Integer> findMaxSequence(ManagerType managerType, UUID hubId);
}