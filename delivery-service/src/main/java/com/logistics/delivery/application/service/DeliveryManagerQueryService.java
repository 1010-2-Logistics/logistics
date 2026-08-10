package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.query.SearchDeliveryManagerQuery;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryManagerQueryService {

    private final DeliveryManagerRepository deliveryManagerRepository;

    public DeliveryManager getById(Long deliveryManagerId, UserPrincipal principal) {
        DeliveryManager manager = deliveryManagerRepository.findByIdAndDeletedAtIsNull(deliveryManagerId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        if (principal.getRole() != Role.MASTER) {
            boolean owns = switch (principal.getRole()) {
                case HUB_MANAGER -> manager.getHubId() != null && principal.getHubId().equals(manager.getHubId());
                case HUB_DELIVERY_MANAGER, COMPANY_DELIVERY_MANAGER ->
                        principal.getUserId().equals(manager.getDeliveryManagerId());
                default -> false;
            };
            if (!owns) {
                throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
            }
        }
        return manager;
    }

    public Page<DeliveryManager> search(SearchDeliveryManagerQuery query, UserPrincipal principal) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));

        // 목록 조회는 MASTER(전체)/HUB_MANAGER(담당 허브로 강제 스코프)만 허용.
        // [x]_DELIVERY_MANAGER는 "본인 정보만"이 목록 API 성격과 안 맞아 getById로 유도한다.
        if (principal.getRole() == Role.MASTER) {
            return deliveryManagerRepository.search(query.managerType(), query.hubId(), pageRequest);
        }
        if (principal.getRole() == Role.HUB_MANAGER) {
            return deliveryManagerRepository.search(query.managerType(), principal.getHubId(), pageRequest);
        }
        throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }
}