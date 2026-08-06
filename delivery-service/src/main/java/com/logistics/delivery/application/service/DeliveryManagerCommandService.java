package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.command.RegisterDeliveryManagerCommand;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.feign.client.HubClient;
import feign.FeignException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryManagerCommandService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final DeliveryManagerAssignmentStateRepository assignmentStateRepository;
    private final HubClient hubClient;

    public DeliveryManager register(RegisterDeliveryManagerCommand command) {
        if (deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(command.userId())) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_ALREADY_EXISTS);
        }

        if (command.managerType() == ManagerType.COMPANY_DELIVERY_MANAGER) {
            validateHub(command.hubId());
        }

        int nextSequence = deliveryManagerRepository
                .findMaxSequence(command.managerType(), command.hubId())
                .map(seq -> seq + 1)
                .orElse(0);

        DeliveryManager manager = DeliveryManager.create(
                command.userId(), command.hubId(), command.slackId(), command.managerType(), nextSequence);
        DeliveryManager saved = deliveryManagerRepository.save(manager);

        ensureAssignmentStateExists(command.managerType(), command.hubId());

        return saved;
    }

    private void validateHub(UUID hubId) {
        if (hubId == null) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
        }
        try {
            Set<UUID> validIds = hubClient.validateHubIds(List.of(hubId));
            if (!validIds.contains(hubId)) {
                throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
            }
        } catch (FeignException e) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }

    private void ensureAssignmentStateExists(ManagerType managerType, UUID hubId) {
        if (assignmentStateRepository.findForUpdate(managerType, hubId).isEmpty()) {
            assignmentStateRepository.save(DeliveryManagerAssignmentState.init(managerType, hubId));
        }
    }


    public DeliveryManager update(Long deliveryManagerId, UUID hubId) {
        DeliveryManager manager = deliveryManagerRepository.findByIdAndDeletedAtIsNull(deliveryManagerId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        if (manager.getManagerType() == ManagerType.COMPANY_DELIVERY_MANAGER) {
            validateHub(hubId);
        }

        manager.updateHub(hubId);
        return manager;
    }
    public void delete(Long deliveryManagerId) {
        DeliveryManager manager = deliveryManagerRepository.findByIdAndDeletedAtIsNull(deliveryManagerId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND));
        manager.markDeleted(null);
    }
}