package com.logistics.slack.application.authorization;


import com.logistics.slack.application.dto.auth.AuthenticatedUser;
import com.logistics.slack.application.dto.result.UserInfo;
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
    // 메시지 생성 시 수신자 기준 권한 검증
    public void validateAccess(
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

        if ((authenticatedUser.role() == Role.HUB_DELIVERY_MANAGER
                || authenticatedUser.role() == Role.COMPANY_DELIVERY_MANAGER)
                && referenceId != null) {
            // 업무 연관성 검증은 Order/Delivery 조회 계약 추가 후 검증
            return;
        }

        throw new CustomException(
                CommonErrorCode.AUTH_FORBIDDEN
        );
    }

    // 단건 조회 / 재발송 등 Slack 이력 접근 검증
    public void validateAccess(
            AuthenticatedUser authenticatedUser,
            Slack slack
    ) {
        if (authenticatedUser.role() == Role.MASTER) {
            return;
        }

        if (authenticatedUser.userId().equals(slack.getSenderId())
                || authenticatedUser.userId().equals(slack.getReceiverId())) {
            return;
        }

        throw new CustomException(
                CommonErrorCode.AUTH_FORBIDDEN
        );
    }

    // 삭제 권한 검증
    public void validateAccess(
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

        return authenticatedUser.userId().equals(slack.getSenderId())
                || authenticatedUser.userId().equals(slack.getReceiverId());
    }
}