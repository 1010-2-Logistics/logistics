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
        // MASTER: 특정 허브에 소속되지 않는다
        if (this.role == Role.MASTER && this.hubId != null) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }
        // HUB_MANAGER: 담당 허브 스코핑에 반드시 필요하므로 hubId 필수
        if (this.role == Role.HUB_MANAGER && this.hubId == null) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }
        // HUB_DELIVERY_MANAGER는 구간마다 이동하므로 고정 허브가 없다(hubId null이 정상).
        // DeliveryManager 엔티티도 "허브 배송담당자는 hubId null"을 강제한다.
        // company-service와 규칙이 갈리는 지점 — delivery 도메인 모델 기준을 따른다.
    }
}