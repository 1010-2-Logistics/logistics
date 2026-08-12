package com.logistics.slack.application.authorization;

import com.logistics.slack.application.dto.auth.AuthenticatedUser;
import com.logistics.slack.domain.entity.Role;
import com.logistics.slack.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlackAuthorizationServiceTest {
    private final Long userId = 1L;

    private final SlackAuthorizationService slackAuthorizationService = new SlackAuthorizationService();

    @Nested
    @DisplayName("슬랙 메시지 생성 권한")
    class slack_create_authorization {
        @Test
        @DisplayName("MASTER는 슬랙 메시지 생성 가능")
        void slack_create_master_success() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.MASTER,
                    null,
                    null
            );

            slackAuthorizationService.validateCreateAccess(
                    authenticatedUser
            );
        }

        @Test
        @DisplayName("허브 관리자는 슬랙 메시지 생성 가능")
        void slack_create_hub_manager_success() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.HUB_MANAGER,
                    null,
                    null
            );

            slackAuthorizationService.validateCreateAccess(
                    authenticatedUser
            );
        }

        @Test
        @DisplayName("허브 배송 담당자는 슬랙 메시지 생성 가능")
        void slack_create_hub_delivery_manager_success() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.HUB_DELIVERY_MANAGER,
                    null,
                    null
            );

            slackAuthorizationService.validateCreateAccess(
                    authenticatedUser
            );
        }

        @Test
        @DisplayName("업체 관리자는 슬랙 메시지 생성 가능")
        void slack_create_company_manager_success() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.COMPANY_MANAGER,
                    null,
                    null
            );

            slackAuthorizationService.validateCreateAccess(
                    authenticatedUser
            );
        }

        @Test
        @DisplayName("업체 배송 담당자는 슬랙 메시지 생성 가능")
        void slack_create_company_delivery_manager_success() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.COMPANY_DELIVERY_MANAGER,
                    null,
                    null
            );

            slackAuthorizationService.validateCreateAccess(
                    authenticatedUser
            );
        }

        @Test
        @DisplayName("인증 정보가 없으면 슬랙 메시지 생성 불가")
        void slack_create_unauthenticated() {
            assertThatThrownBy(() ->
                    slackAuthorizationService.validateCreateAccess(
                            null
                    )
            ).isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("슬랙 메시지 조회 권한")
    class slack_read_authorization {

        @Test
        @DisplayName("MASTER는 슬랙 메시지 조회 가능")
        void slack_read_master_success() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.MASTER,
                    null,
                    null
            );

            slackAuthorizationService.validateReadAccess(
                    authenticatedUser
            );
        }

        @Test
        @DisplayName("MASTER가 아니면 슬랙 메시지 조회 불가")
        void slack_read_not_master() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.HUB_MANAGER,
                    null,
                    null
            );

            assertThatThrownBy(() ->
                    slackAuthorizationService.validateReadAccess(
                            authenticatedUser
                    )
            ).isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("슬랙 메시지 재발송 권한")
    class slack_retry_authorization {

        @Test
        @DisplayName("MASTER는 슬랙 메시지 재발송 가능")
        void slack_retry_master_success() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.MASTER,
                    null,
                    null
            );

            slackAuthorizationService.validateRetryAccess(
                    authenticatedUser
            );
        }

        @Test
        @DisplayName("MASTER가 아니면 슬랙 메시지 재발송 불가")
        void slack_retry_not_master() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.HUB_MANAGER,
                    null,
                    null
            );

            assertThatThrownBy(() ->
                    slackAuthorizationService.validateRetryAccess(
                            authenticatedUser
                    )
            ).isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("슬랙 메시지 삭제 권한")
    class slack_delete_authorization {

        @Test
        @DisplayName("MASTER는 슬랙 메시지 삭제 가능")
        void slack_delete_master_success() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.MASTER,
                    null,
                    null
            );

            slackAuthorizationService.validateDeleteAccess(
                    authenticatedUser
            );
        }

        @Test
        @DisplayName("MASTER가 아니면 슬랙 메시지 삭제 불가")
        void slack_delete_not_master() {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.HUB_MANAGER,
                    null,
                    null
            );

            assertThatThrownBy(() ->
                    slackAuthorizationService.validateDeleteAccess(
                            authenticatedUser
                    )
            ).isInstanceOf(CustomException.class);
        }
    }
}