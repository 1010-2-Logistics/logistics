package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.auth.AuthenticatedUser;
import com.logistics.inventory.domain.entity.Role;
import com.logistics.inventory.global.exception.CommonErrorCode;
import com.logistics.inventory.global.exception.CustomException;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class InventoryAuthorizationService {
    public void validateHubAccess(
            AuthenticatedUser authenticatedUser,
            UUID inventoryHubId
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return;
        }

        if (authenticatedUser.role() == Role.HUB_MANAGER
                && inventoryHubId.equals(authenticatedUser.hubId())) {
            return;
        }

        throw new CustomException(
                CommonErrorCode.AUTH_FORBIDDEN
        );
    }
}
