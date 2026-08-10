package com.logistics.delivery.domain.repository;

import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryManagerRepository {

    DeliveryManager save(DeliveryManager manager);

    boolean existsByDeliveryManagerIdAndDeletedAtIsNull(Long deliveryManagerId);

    // 신규 등록 시 "마지막 순번" 조회용 (허브담당자=전체기준, 업체담당자=허브별기준)
    Optional<Integer> findMaxSequence(ManagerType managerType, UUID hubId);

    Optional<DeliveryManager> findByIdAndDeletedAtIsNull(Long deliveryManagerId);

    Optional<DeliveryManager> findNextCandidate(ManagerType managerType, UUID hubId, int afterSequence);

    Optional<DeliveryManager> findFirstCandidate(ManagerType managerType, UUID hubId);

    Page<DeliveryManager> search(ManagerType managerType, UUID hubId, Pageable pageable);
}