package com.logistics.slack.application.authorization;


import com.logistics.slack.application.dto.auth.AuthenticatedUser;
import com.logistics.slack.application.dto.result.UserInfo;
import com.logistics.slack.application.port.UserPort;
import com.logistics.slack.domain.entity.Role;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.global.exception.CommonErrorCode;
import com.logistics.slack.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SlackAuthorizationService {
    private final UserPort userPort;

    // 메시지 생성 시 수신자 기준 권한 검증
    public void validateCreateAccess(
            AuthenticatedUser authenticatedUser,
            UserInfo receiver,
            UUID referenceId
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return;
        }

        if (authenticatedUser.role() == Role.HUB_MANAGER
                && authenticatedUser.hubId() != null
                && authenticatedUser.hubId().equals(receiver.hubId())) {
            return;
        }

        if (authenticatedUser.role() == Role.COMPANY_MANAGER
                && authenticatedUser.companyId() != null
                && authenticatedUser.companyId().equals(receiver.companyId())) {
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

            throw new CustomException(
                    CommonErrorCode.AUTH_FORBIDDEN
            );
        }

        UserInfo sender = userPort.getUser(slack.getSenderId());
        UserInfo receiver = userPort.getUser(slack.getReceiverId());

        if (authenticatedUser.role() == Role.HUB_MANAGER
                && authenticatedUser.hubId() != null
                && (authenticatedUser.hubId().equals(sender.hubId())
                || authenticatedUser.hubId().equals(receiver.hubId()))) {
            return;
        }

        if (authenticatedUser.role() == Role.COMPANY_MANAGER
                && authenticatedUser.companyId() != null
                && (authenticatedUser.companyId().equals(sender.companyId())
                || authenticatedUser.companyId().equals(receiver.companyId()))) {
            return;
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

            throw new CustomException(
                    CommonErrorCode.AUTH_FORBIDDEN
            );
        }

        UserInfo sender = userPort.getUser(slack.getSenderId());
        UserInfo receiver = userPort.getUser(slack.getReceiverId());

        if (authenticatedUser.role() == Role.HUB_MANAGER
                && authenticatedUser.hubId() != null
                && (authenticatedUser.hubId().equals(sender.hubId())
                || authenticatedUser.hubId().equals(receiver.hubId()))) {
            return;
        }

        if (authenticatedUser.role() == Role.COMPANY_MANAGER
                && authenticatedUser.companyId() != null
                && (authenticatedUser.companyId().equals(sender.companyId())
                || authenticatedUser.companyId().equals(receiver.companyId()))) {
            return;
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

    // 목록 조회 시 현재 사용자가 해당 Slack 이력을 볼 수 있는지 판단
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

        UserInfo sender = userPort.getUser(slack.getSenderId());
        UserInfo receiver = userPort.getUser(slack.getReceiverId());

        if (authenticatedUser.role() == Role.HUB_MANAGER
                && authenticatedUser.hubId() != null) {

            return authenticatedUser.hubId().equals(sender.hubId())
                    || authenticatedUser.hubId().equals(receiver.hubId());
        }

        if (authenticatedUser.role() == Role.COMPANY_MANAGER
                && authenticatedUser.companyId() != null) {

            return authenticatedUser.companyId().equals(sender.companyId())
                    || authenticatedUser.companyId().equals(receiver.companyId());
        }

        return false;
    }
}