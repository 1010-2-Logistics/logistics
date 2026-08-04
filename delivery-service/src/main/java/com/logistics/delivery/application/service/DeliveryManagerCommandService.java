package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.command.RegisterDeliveryManagerCommand;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.feign.client.HubClient;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryManagerCommandService {

    private final DeliveryManagerRepository deliveryManagerRepository;
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

        return deliveryManagerRepository.save(manager);
    }

    private void validateHub(UUID hubId) {
        if (hubId == null) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
        }
        try {
            hubClient.getHub(hubId);
        } catch (FeignException.NotFound e) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
        } catch (FeignException e) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }

    public void delete(Long deliveryManagerId) {
        DeliveryManager manager = deliveryManagerRepository.findByIdAndDeletedAtIsNull(deliveryManagerId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND));
        manager.markDeleted(null);
    }
}