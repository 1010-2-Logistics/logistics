package com.logistics.order.application.authorization;


import com.logistics.order.application.dto.auth.AuthenticatedUser;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.entity.Role;
import com.logistics.order.global.exception.CommonErrorCode;
import com.logistics.order.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderAuthorizationService {
    // 접근 가능한 역할인 건 알겠는데, 이 HUB_MANAGER가 이 주문의 담당 허브관리자가 맞나? 역할임

    // 수정/삭제/취소용 HUB_MANAGER 검증
    public void validateHubAccess(
            AuthenticatedUser authenticatedUser,
            UUID orderHubId
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return;
        }

        if (authenticatedUser.role() == Role.HUB_MANAGER
                && orderHubId.equals(authenticatedUser.hubId())) {
            return;
        }

        throw new CustomException(
                CommonErrorCode.AUTH_FORBIDDEN
        );
    }

    public void validateReadAccess(
            AuthenticatedUser authenticatedUser,
            Order order,
            UUID orderHubId,
            Long deliveryManagerId
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return;
        }

        if (authenticatedUser.role() == Role.HUB_MANAGER
                && orderHubId.equals(authenticatedUser.hubId())) {
            return;
        }

        if (authenticatedUser.role() == Role.COMPANY_MANAGER
                && (order.getStartCompanyId().equals(authenticatedUser.companyId())
                || order.getEndCompanyId().equals(authenticatedUser.companyId()))) {
            return;
        }

        if (authenticatedUser.role() == Role.COMPANY_DELIVERY_MANAGER
                && authenticatedUser.userId().equals(deliveryManagerId)) {
            return;
        }

        throw new CustomException(
                CommonErrorCode.AUTH_FORBIDDEN
        );
    }
}
