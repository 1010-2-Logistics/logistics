package com.logistics.delivery.infrastructure.security.principal;

import java.util.UUID;
import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final Long userId;
    private final Role role;
    private final UUID hubId;
    private final UUID companyId;

    public static UserPrincipal from(String userId, String role, String hubId, String companyId) {
        Long parsedUserId;
        Role parsedRole = null;
        UUID parsedHubId = null;
        UUID parsedCompanyId = null;

        try {
            if (userId == null || userId.isBlank()) {
                return null;
            }
            parsedUserId = Long.parseLong(userId);

            if (role != null && !role.isBlank()) {
                parsedRole = Role.valueOf(role);
            }
            if (hubId != null && !hubId.isBlank()) {
                parsedHubId = UUID.fromString(hubId);
            }
            if (companyId != null && !companyId.isBlank()) {
                parsedCompanyId = UUID.fromString(companyId);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }

        return new UserPrincipal(parsedUserId, parsedRole, parsedHubId, parsedCompanyId);
    }

    public void validateRoleConstraints() {
        // MASTER: hubId 없어야 함
        if (this.role == Role.MASTER && this.hubId != null) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }
        // HUB_MANAGER, HUB_DELIVERY_MANAGER: hubId 있어야 함
        if ((this.role == Role.HUB_MANAGER || this.role == Role.HUB_DELIVERY_MANAGER) && this.hubId == null) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }
        // COMPANY_MANAGER, COMPANY_DELIVERY_MANAGER: companyId 조건 없이 hubId만 확인
        // (delivery-service엔 companyId 개념이 없어 company-service 규칙과 다르게 감)
    }
}