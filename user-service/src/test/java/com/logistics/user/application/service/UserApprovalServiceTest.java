package com.logistics.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.logistics.user.application.dto.command.ChangeApprovalCommandDto;
import com.logistics.user.application.dto.result.ChangeApprovalResult;
import com.logistics.user.domain.entity.ApprovalDecision;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 사용자 가입 승인·거절 유스케이스 단위 테스트.
 *
 * 실제 DB와 Spring Context를 실행하지 않고
 * UserApprovalService의 권한 검증과 상태 변경 흐름만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class UserApprovalServiceTest {

    /**
     * 사용자 조회를 담당하는 Repository Mock.
     */
    @Mock
    private UserQueryRepository userQueryRepository;

    /**
     * 테스트 대상 서비스.
     */
    private UserApprovalService userApprovalService;

    @BeforeEach
    void setUp() {
        userApprovalService =
                new UserApprovalService(userQueryRepository);
    }

    @Test
    @DisplayName("MASTER는 PENDING 사용자를 승인할 수 있다")
    void approve_success_byMaster() {
        // given
        User master = createMaster(1L);

        User targetUser = createHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        15L,
                        1L,
                        ApprovalDecision.APPROVE
                );

        /*
         * processedBy = 1인 관리자를 조회하면 MASTER를 반환한다.
         */
        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(1L)
        ).willReturn(Optional.of(master));

        /*
         * targetUserId = 15인 사용자를 조회하면
         * PENDING 상태의 대상 사용자를 반환한다.
         */
        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(15L)
        ).willReturn(Optional.of(targetUser));

        // when
        ChangeApprovalResult result =
                userApprovalService.changeApproval(command);

        // then
        assertThat(result.userId())
                .isEqualTo(15L);

        assertThat(result.previousStatus())
                .isEqualTo(UserStatus.PENDING);

        assertThat(result.status())
                .isEqualTo(UserStatus.APPROVED);

        assertThat(result.processedBy())
                .isEqualTo(1L);

        assertThat(result.processedAt())
                .isNotNull();

        /*
         * 반환값만 변경된 것이 아니라
         * 실제 User 객체의 상태도 변경되었는지 확인한다.
         */
        assertThat(targetUser.getStatus())
                .isEqualTo(UserStatus.APPROVED);
    }

    @Test
    @DisplayName("MASTER는 PENDING 사용자를 거절할 수 있다")
    void reject_success_byMaster() {
        // given
        User master = createMaster(1L);

        User targetUser = createHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        15L,
                        1L,
                        ApprovalDecision.REJECT
                );

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(1L)
        ).willReturn(Optional.of(master));

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(15L)
        ).willReturn(Optional.of(targetUser));

        // when
        ChangeApprovalResult result =
                userApprovalService.changeApproval(command);

        // then
        assertThat(result.previousStatus())
                .isEqualTo(UserStatus.PENDING);

        assertThat(result.status())
                .isEqualTo(UserStatus.REJECTED);

        assertThat(targetUser.getStatus())
                .isEqualTo(UserStatus.REJECTED);
    }

    @Test
    @DisplayName("HUB_MANAGER는 같은 허브 사용자를 승인할 수 있다")
    void approve_success_byHubManager_inSameHub() {
        // given
        UUID hubId = UUID.randomUUID();

        User hubManager = createHubManager(
                2L,
                hubId
        );

        User targetUser = createHubDeliveryManager(
                15L,
                hubId
        );

        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        15L,
                        2L,
                        ApprovalDecision.APPROVE
                );

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(2L)
        ).willReturn(Optional.of(hubManager));

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(15L)
        ).willReturn(Optional.of(targetUser));

        // when
        ChangeApprovalResult result =
                userApprovalService.changeApproval(command);

        // then
        assertThat(result.status())
                .isEqualTo(UserStatus.APPROVED);

        assertThat(targetUser.getStatus())
                .isEqualTo(UserStatus.APPROVED);
    }

    @Test
    @DisplayName("HUB_MANAGER는 다른 허브 사용자를 승인할 수 없다")
    void approve_fail_byHubManager_inDifferentHub() {
        // given
        User hubManager = createHubManager(
                2L,
                UUID.randomUUID()
        );

        User targetUser = createHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        15L,
                        2L,
                        ApprovalDecision.APPROVE
                );

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(2L)
        ).willReturn(Optional.of(hubManager));

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(15L)
        ).willReturn(Optional.of(targetUser));

        // when & then
        assertThatThrownBy(
                () -> userApprovalService
                        .changeApproval(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(
                            customException.getErrorCode()
                    ).isEqualTo(
                            UserErrorCode
                                    .USER_APPROVAL_ACCESS_DENIED
                    );
                });

        /*
         * 권한 검증에 실패했으므로 상태는 그대로 PENDING이다.
         */
        assertThat(targetUser.getStatus())
                .isEqualTo(UserStatus.PENDING);
    }

    @Test
    @DisplayName("HUB_MANAGER는 MASTER 가입 신청을 승인할 수 없다")
    void approve_fail_whenHubManagerProcessesMaster() {
        // given
        UUID hubId = UUID.randomUUID();

        User hubManager = createHubManager(
                2L,
                hubId
        );

        User targetMaster = createMaster(15L);

        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        15L,
                        2L,
                        ApprovalDecision.APPROVE
                );

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(2L)
        ).willReturn(Optional.of(hubManager));

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(15L)
        ).willReturn(Optional.of(targetMaster));

        // when & then
        assertThatThrownBy(
                () -> userApprovalService
                        .changeApproval(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(
                            customException.getErrorCode()
                    ).isEqualTo(
                            UserErrorCode
                                    .USER_APPROVAL_ACCESS_DENIED
                    );
                });
    }

    @Test
    @DisplayName("일반 사용자는 가입 신청을 처리할 수 없다")
    void approve_fail_byNonManager() {
        // given
        UUID hubId = UUID.randomUUID();

        User deliveryManager =
                createHubDeliveryManager(
                        3L,
                        hubId
                );

        User targetUser =
                createHubDeliveryManager(
                        15L,
                        hubId
                );

        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        15L,
                        3L,
                        ApprovalDecision.APPROVE
                );

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(3L)
        ).willReturn(Optional.of(deliveryManager));

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(15L)
        ).willReturn(Optional.of(targetUser));

        // when & then
        assertThatThrownBy(
                () -> userApprovalService
                        .changeApproval(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(
                            customException.getErrorCode()
                    ).isEqualTo(
                            UserErrorCode
                                    .USER_APPROVAL_ACCESS_DENIED
                    );
                });
    }

    @Test
    @DisplayName("이미 APPROVED 상태인 사용자는 다시 승인할 수 없다")
    void approve_fail_whenAlreadyApproved() {
        // given
        User master = createMaster(1L);

        User targetUser = createHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        /*
         * 테스트 준비를 위해 먼저 승인 상태로 변경한다.
         */
        targetUser.approve();

        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        15L,
                        1L,
                        ApprovalDecision.APPROVE
                );

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(1L)
        ).willReturn(Optional.of(master));

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(15L)
        ).willReturn(Optional.of(targetUser));

        // when & then
        assertThatThrownBy(
                () -> userApprovalService
                        .changeApproval(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(
                            customException.getErrorCode()
                    ).isEqualTo(
                            UserErrorCode
                                    .USER_APPROVAL_CONFLICT
                    );
                });
    }

    @Test
    @DisplayName("이미 REJECTED 상태인 사용자는 승인할 수 없다")
    void approve_fail_whenAlreadyRejected() {
        // given
        User master = createMaster(1L);

        User targetUser = createHubDeliveryManager(
                15L,
                UUID.randomUUID()
        );

        /*
         * 대상 사용자를 먼저 거절 상태로 만든다.
         */
        targetUser.reject();

        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        15L,
                        1L,
                        ApprovalDecision.APPROVE
                );

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(1L)
        ).willReturn(Optional.of(master));

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(15L)
        ).willReturn(Optional.of(targetUser));

        // when & then
        assertThatThrownBy(
                () -> userApprovalService
                        .changeApproval(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(
                            customException.getErrorCode()
                    ).isEqualTo(
                            UserErrorCode
                                    .USER_APPROVAL_CONFLICT
                    );
                });
    }

    @Test
    @DisplayName("대상 사용자가 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다")
    void approve_fail_whenTargetUserNotFound() {
        // given
        User master = createMaster(1L);

        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        999L,
                        1L,
                        ApprovalDecision.APPROVE
                );

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(1L)
        ).willReturn(Optional.of(master));

        given(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(999L)
        ).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> userApprovalService
                        .changeApproval(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(
                            customException.getErrorCode()
                    ).isEqualTo(
                            UserErrorCode.USER_NOT_FOUND
                    );
                });
    }

    @Test
    @DisplayName("사용자 ID가 양수가 아니면 승인 요청 형식 오류가 발생한다")
    void approve_fail_whenTargetUserIdIsInvalid() {
        // given
        ChangeApprovalCommandDto command =
                new ChangeApprovalCommandDto(
                        0L,
                        1L,
                        ApprovalDecision.APPROVE
                );

        // when & then
        assertThatThrownBy(
                () -> userApprovalService
                        .changeApproval(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(
                            customException.getErrorCode()
                    ).isEqualTo(
                            UserErrorCode
                                    .USER_APPROVAL_INVALID_REQUEST
                    );
                });
    }

    /**
     * MASTER 테스트 객체를 생성한다.
     */
    private User createMaster(Long userId) {
        User user = User.create(
                "master01",
                "encodedPassword",
                "U_MASTER_" + userId,
                UserRole.MASTER,
                null,
                null
        );

        setUserId(user, userId);

        return user;
    }

    /**
     * HUB_MANAGER 테스트 객체를 생성한다.
     */
    private User createHubManager(
            Long userId,
            UUID hubId
    ) {
        User user = User.create(
                "hub" + userId,
                "encodedPassword",
                "U_HUB_" + userId,
                UserRole.HUB_MANAGER,
                null,
                hubId
        );

        setUserId(user, userId);

        return user;
    }

    /**
     * HUB_DELIVERY_MANAGER 테스트 객체를 생성한다.
     */
    private User createHubDeliveryManager(
            Long userId,
            UUID hubId
    ) {
        User user = User.create(
                "delivery" + userId,
                "encodedPassword",
                "U_DELIVERY_" + userId,
                UserRole.HUB_DELIVERY_MANAGER,
                null,
                hubId
        );

        setUserId(user, userId);

        return user;
    }

    /**
     * userId는 DB가 생성하는 값이므로 일반 코드에서는 직접 넣지 않는다.
     *
     * 단위 테스트에서는 DB를 사용하지 않기 때문에
     * ReflectionTestUtils를 이용하여 식별자를 설정한다.
     */
    private void setUserId(
            User user,
            Long userId
    ) {
        ReflectionTestUtils.setField(
                user,
                "userId",
                userId
        );
    }
}