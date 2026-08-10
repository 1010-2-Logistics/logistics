package com.logistics.delivery.domain.repository;

import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryManagerAssignmentStateRepository {

    // 동시성 제어를 위한 락 조회 (SELECT ... FOR UPDATE)
    Optional<DeliveryManagerAssignmentState> findForUpdate(ManagerType managerType, UUID hubId);

    DeliveryManagerAssignmentState save(DeliveryManagerAssignmentState state);
}
