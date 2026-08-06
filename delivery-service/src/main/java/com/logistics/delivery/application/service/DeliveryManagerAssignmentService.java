package com.logistics.delivery.application.service;

import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryManagerAssignmentService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final DeliveryManagerAssignmentStateRepository assignmentStateRepository;

    // managerType이 HUB_DELIVERY_MANAGER면 hubId는 항상 null로 호출 (전역 풀)
    public DeliveryManager assignNextManager(ManagerType managerType, UUID hubId) {
        DeliveryManagerAssignmentState state = assignmentStateRepository
                .findForUpdate(managerType, hubId)
                .orElseGet(() -> DeliveryManagerAssignmentState.init(managerType, hubId));

        DeliveryManager nextManager = deliveryManagerRepository
                .findNextCandidate(managerType, hubId, state.getLastAssignedSequence())
                .or(() -> deliveryManagerRepository.findFirstCandidate(managerType, hubId))
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_UNAVAILABLE));

        state.assign(nextManager);
        assignmentStateRepository.save(state);

        return nextManager;
    }
}
