package com.logistics.slack.application.authorization;


import com.logistics.slack.application.dto.auth.AuthenticatedUser;
import com.logistics.slack.domain.entity.Role;
import com.logistics.slack.global.exception.CommonErrorCode;
import com.logistics.slack.global.exception.CustomException;
import org.springframework.stereotype.Component;

@Component
public class SlackAuthorizationService {
    public void validateCreateAccess(
            AuthenticatedUser authenticatedUser
    ) {
        validateAuthenticated(authenticatedUser);
    }

    public void validateReadAccess(
            AuthenticatedUser authenticatedUser
    ) {
        validateMaster(authenticatedUser);
    }

    public void validateRetryAccess(
            AuthenticatedUser authenticatedUser
    ) {
        validateMaster(authenticatedUser);
    }

    public void validateDeleteAccess(
            AuthenticatedUser authenticatedUser
    ) {        validateMaster(authenticatedUser);
    }

    private void validateAuthenticated(
            AuthenticatedUser authenticatedUser
    ) {
        if (authenticatedUser == null
                || authenticatedUser.userId() == null
                || authenticatedUser.role() == null) {
            throw new CustomException(
                    CommonErrorCode.AUTH_FORBIDDEN
            );
        }
    }

    private void validateMaster(
            AuthenticatedUser authenticatedUser
    ) {
        validateAuthenticated(authenticatedUser);

        if (authenticatedUser.role() != Role.MASTER) {
            throw new CustomException(
                    CommonErrorCode.AUTH_FORBIDDEN
            );
        }
    }
}