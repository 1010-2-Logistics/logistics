package com.logistics.slack.application.authorization;


import com.logistics.slack.application.dto.auth.AuthenticatedUser;
import com.logistics.slack.domain.entity.Role;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.global.exception.CommonErrorCode;
import com.logistics.slack.global.exception.CustomException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SlackAuthorizationService {
    public void validateCreateAccess(
            AuthenticatedUser authenticatedUser,
            UUID referenceId
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return;
        }

        if (authenticatedUser.role() == Role.HUB_DELIVERY_MANAGER
                || authenticatedUser.role() == Role.COMPANY_DELIVERY_MANAGER) {

            if (referenceId != null) {
                return;
            }
        }

        throw new CustomException(
                CommonErrorCode.AUTH_FORBIDDEN
        );
    }

    // 단건 조회 권한 검증
    public void validateReadAccess(
            AuthenticatedUser authenticatedUser,
            Slack slack
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return;
        }

        if (authenticatedUser.role() == Role.HUB_DELIVERY_MANAGER
                || authenticatedUser.role() == Role.COMPANY_DELIVERY_MANAGER) {

            if (authenticatedUser.userId().equals(slack.getSenderId())
                    || authenticatedUser.userId().equals(slack.getReceiverId())) {
                return;
            }
        }

        throw new CustomException(
                CommonErrorCode.AUTH_FORBIDDEN
        );
    }

    // 재발송 권한 검증
    public void validateRetryAccess(
            AuthenticatedUser authenticatedUser,
            Slack slack
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return;
        }

        if (authenticatedUser.role() == Role.HUB_DELIVERY_MANAGER
                || authenticatedUser.role() == Role.COMPANY_DELIVERY_MANAGER) {

            if (authenticatedUser.userId().equals(slack.getSenderId())) {
                return;
            }
        }

        throw new CustomException(
                CommonErrorCode.AUTH_FORBIDDEN
        );
    }

    // 삭제 권한 검증
    public void validateDeleteAccess(
            AuthenticatedUser authenticatedUser
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return;
        }

        throw new CustomException(
                CommonErrorCode.AUTH_FORBIDDEN
        );
    }

    // 목록 조회 권한 검증
    public boolean canRead(
            AuthenticatedUser authenticatedUser,
            Slack slack
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return true;
        }

        if (authenticatedUser.role() == Role.HUB_DELIVERY_MANAGER
                || authenticatedUser.role() == Role.COMPANY_DELIVERY_MANAGER) {

            return authenticatedUser.userId().equals(slack.getSenderId())
                    || authenticatedUser.userId().equals(slack.getReceiverId());
        }

        return false;
    }
}