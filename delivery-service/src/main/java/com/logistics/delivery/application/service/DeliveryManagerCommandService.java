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
import feign.RetryableException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    /**
     * user-service에 소속을 동기화한다.
     *
     * <p>소속 변경은 목표 상태를 지정하는 PATCH라 멱등하므로, 응답을 받지 못한 경우
     * FeignConfig의 Retryer가 재시도한다. 재시도가 성공하면 이미 반영된 요청은 409로
     * 돌아오고 아래에서 성공으로 처리된다.
     */
    private void syncUserAffiliation(Long userId, ManagerType managerType, UUID hubId) {
        try {
            userAffiliationPort.changeAffiliation(userId, managerType.name(), hubId);
        } catch (FeignException.Conflict e) {
            // user-service에 이미 동일한 소속으로 등록돼 있음 = 목표 상태 달성이므로 성공 처리(멱등)
        } catch (RetryableException e) {
            // 재시도 후에도 응답을 받지 못했다. user-service 반영 여부를 알 수 없으므로
            // 예외를 던져 로컬 트랜잭션을 롤백한다. 커밋해두면 "delivery에는 담당자가
            // 있는데 user는 그대로"인 상태가 성공으로 확정돼 영영 드러나지 않는다.
            // 롤백하면 클라이언트가 재시도할 때 멱등한 PATCH가 다시 나가 수렴한다.
            log.error("user-service 소속 동기화 결과 미상. userId={}, managerType={}, hubId={}",
                    userId, managerType, hubId, e);
            throw new CustomException(DeliveryErrorCode.DELIVERY_USER_SERVICE_UNAVAILABLE);
        } catch (FeignException e) {
            // 상태 코드를 받았다 = 서버가 응답했고 결과가 확정됐다
            log.warn("user-service 소속 동기화 실패. userId={}, status={}, message={}",
                    userId, e.status(), e.getMessage());
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
            log.warn("hub-service 허브 검증 실패. hubId={}, status={}", hubId, e.status());
            throw new CustomException(DeliveryErrorCode.DELIVERY_HUB_SERVICE_UNAVAILABLE);
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