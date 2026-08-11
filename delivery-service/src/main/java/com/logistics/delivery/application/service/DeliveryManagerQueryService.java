package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.query.SearchDeliveryManagerQuery;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

        if (principal.getRole() == Role.MASTER) {
            return deliveryManagerRepository.search(query.managerType(), query.hubId(), pageRequest);
        }
        if (principal.getRole() == Role.HUB_MANAGER) {
            // 쿼리로 들어온 hubId는 무시하고 본인 담당 허브로 강제 스코프
            return deliveryManagerRepository.search(query.managerType(), principal.getHubId(), pageRequest);
        }
        // 배송담당자는 본인 정보만 조회 가능 (명세상 R 권한 보유)
        DeliveryManager me = deliveryManagerRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND));
        return new PageImpl<>(List.of(me), pageRequest, 1);
    }
}