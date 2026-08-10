package com.logistics.order.application.authorization;


import com.logistics.order.application.dto.auth.AuthenticatedUser;
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
}
