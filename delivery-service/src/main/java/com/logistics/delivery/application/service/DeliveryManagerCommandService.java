package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.command.RegisterDeliveryManagerCommand;
import com.logistics.delivery.application.port.HubPort;
import com.logistics.delivery.application.port.UserAffiliationPort;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
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
    private final HubPort hubPort;
    private final UserAffiliationPort userAffiliationPort;

    public DeliveryManager registerDeliveryManager(RegisterDeliveryManagerCommand command) {
        if (deliveryManagerRepository.existsByDeliveryManagerIdAndDeletedAtIsNull(command.userId())) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_ALREADY_EXISTS);
        }

        if (command.managerType() == ManagerType.COMPANY_DELIVERY_MANAGER) {
            validateHub(command.hubId());
        }

        // 같은 (managerType, hubId) 풀에 대한 시퀀스 채번을 직렬화 + 배정 상태 생성 책임을 등록 쪽으로 확정
        assignmentStateRepository
                .findForUpdate(command.managerType(), command.hubId())
                .orElseGet(() -> assignmentStateRepository.save(
                        DeliveryManagerAssignmentState.init(command.managerType(), command.hubId())));

        int nextSequence = deliveryManagerRepository
                .findMaxSequence(command.managerType(), command.hubId())
                .map(seq -> seq + 1)
                .orElse(0);

        DeliveryManager manager = DeliveryManager.create(
                command.userId(), command.hubId(), command.slackId(), command.managerType(), nextSequence);
        DeliveryManager saved = deliveryManagerRepository.save(manager);

        syncUserAffiliation(saved.getDeliveryManagerId(), saved.getManagerType(), saved.getHubId());

        return saved;
    }

    private void syncUserAffiliation(Long userId, ManagerType managerType, UUID hubId) {
        try {
            userAffiliationPort.changeAffiliation(userId, managerType.name(), hubId);
        } catch (FeignException.Conflict e) {
            // user-service에 이미 동일한 소속으로 등록돼 있음 = 목표 상태 달성이므로 성공 처리(멱등)
        } catch (FeignException e) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_USER_SERVICE_UNAVAILABLE);
        }
    }

    private void validateHub(UUID hubId) {
        if (hubId == null) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
        }
        try {
            Set<UUID> validIds = hubPort.validateHubIds(List.of(hubId));
            if (!validIds.contains(hubId)) {
                throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
            }
        } catch (FeignException e) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_USER_SERVICE_UNAVAILABLE);
        }
    }

    public DeliveryManager updateDeliveryManager(Long deliveryManagerId, UUID hubId) {
        DeliveryManager manager = deliveryManagerRepository.findByIdAndDeletedAtIsNull(deliveryManagerId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        if (manager.getManagerType() == ManagerType.COMPANY_DELIVERY_MANAGER) {
            validateHub(hubId);
        }

        manager.updateHub(hubId);
        syncUserAffiliation(manager.getDeliveryManagerId(), manager.getManagerType(), manager.getHubId());

        return manager;
    }

    public void deleteDeliveryManager(Long deliveryManagerId, UserPrincipal principal) {
        DeliveryManager manager = deliveryManagerRepository.findByIdAndDeletedAtIsNull(deliveryManagerId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND));
        manager.markDeleted(principal.getUserId());
    }
}