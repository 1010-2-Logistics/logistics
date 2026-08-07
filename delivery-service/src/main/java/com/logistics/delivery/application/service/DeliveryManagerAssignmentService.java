package com.logistics.delivery.application.service;

import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryManagerAssignmentService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final DeliveryManagerAssignmentStateRepository assignmentStateRepository;

    public DeliveryManager assignNextManager(ManagerType managerType, UUID hubId) {
        try {
            DeliveryManagerAssignmentState state = assignmentStateRepository
                    .findForUpdate(managerType, hubId)
                    .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_UNAVAILABLE));

            DeliveryManager nextManager = deliveryManagerRepository
                    .findNextCandidate(managerType, hubId, state.getLastAssignedSequence())
                    .or(() -> deliveryManagerRepository.findFirstCandidate(managerType, hubId))
                    .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_UNAVAILABLE));

            state.assign(nextManager);
            assignmentStateRepository.save(state);

            return nextManager;
        } catch (PessimisticLockingFailureException e) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_LOCK_TIMEOUT);
        }
    }
}