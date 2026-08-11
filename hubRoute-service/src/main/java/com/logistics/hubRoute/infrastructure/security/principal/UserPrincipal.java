package com.logistics.hubRoute.infrastructure.security.principal;

import com.logistics.hubRoute.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final Long userId;

    private final Role role;

    private final UUID hubId;

    private final UUID companyId;

    public static UserPrincipal from(String userId, String role, String hubId, String companyId) {
        Long parseUserId = null;
        Role parseRole = null;
        UUID parseHubId = null;
        UUID parseCompanyId = null;

        try {
            if(userId != null && !userId.isBlank())
                parseUserId = Long.parseLong(userId);

            if(role != null && !role.isBlank())
                parseRole = Role.valueOf(role);

            if(hubId != null && !hubId.isBlank())
                parseHubId = UUID.fromString(hubId);

            if(companyId != null && !companyId.isBlank())
                parseCompanyId = UUID.fromString(companyId);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if(parseUserId == null) {
            return null;
        }

        UserPrincipal principal = new UserPrincipal(
                parseUserId,
                parseRole,
                parseHubId,
                parseCompanyId
        );

        return principal;
    }

    public void validateRoleConstraints() {

    }

}
