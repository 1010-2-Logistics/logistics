package com.logistics.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.user.application.dto.command.DeleteUserCommandDto;
import com.logistics.user.application.port.EventPublisher;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.repository.UserCommandRepository;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * MASTER에 의한 사용자 관리자 삭제 기능을 검증한다.
 *
 * 실제 DB는 사용하지 않고 Repository를 Mock으로 대체한다.
 *
 * 검증 대상:
 * - MASTER가 다른 사용자를 삭제할 수 있는지
 * - 삭제 시 deletedAt / deletedBy가 기록되는지
 * - MASTER가 아닌 사용자는 삭제할 수 없는지
 * - MASTER 본인은 자기 자신을 관리자 삭제할 수 없는지
 * - 존재하지 않거나 이미 삭제된 사용자를 삭제할 수 없는지
 */
@ExtendWith(MockitoExtension.class)
class UserCommandServiceDeleteTest {

    @Mock
    private UserCommandRepository userCommandRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private UserQueryRepository userQueryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserCommandService userCommandService;

    /**
     * MASTER가 다른 사용자를 정상적으로 Soft Delete
     */
    @Test
    void MASTER는_다른_사용자를_삭제할_수_있다() {
        // given
        Long masterId = 1L;
        Long targetUserId = 5L;

        /*
         * 삭제 대상 사용자.
         */
        User targetUser = User.create(
                "sample01",
                "홍길동",
                "encoded-password",
                "U0123456789",
                UserRole.MASTER,
                null,
                null
        );

        DeleteUserCommandDto command =
                new DeleteUserCommandDto(
                        masterId,
                        UserRole.MASTER,
                        targetUserId
                );

        when(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(targetUserId)
        ).thenReturn(Optional.of(targetUser));

        // when
        userCommandService.deleteUser(command);

        // then

        /*
         * 실제 삭제 대상 사용자를 Repository에서 조회했는지 확인.
         */
        verify(userQueryRepository)
                .findByIdAndDeletedAtIsNull(targetUserId);

        /*
         * Soft Delete이므로 deletedAt 설정
         */
        assertThat(targetUser.getDeletedAt())
                .isNotNull();

        /*
         * 삭제를 수행한 MASTER의 ID 기록
         */
        assertThat(targetUser.getDeletedBy())
                .isEqualTo(masterId);
    }

    /**
     * MASTER가 아닌 사용자는 관리자 삭제 API를 실행할 수 없다.
     */
    @Test
    void MASTER가_아닌_사용자는_다른_사용자를_삭제할_수_없다() {
        // given
        Long requesterId = 2L;
        Long targetUserId = 5L;

        DeleteUserCommandDto command =
                new DeleteUserCommandDto(
                        requesterId,
                        UserRole.HUB_MANAGER,
                        targetUserId
                );

        // when & then
        assertThatThrownBy(
                () -> userCommandService.deleteUser(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {

                    CustomException customException =
                            (CustomException) exception;

                    assertThat(customException.getErrorCode())
                            .isEqualTo(
                                    UserErrorCode.USER_DELETE_ACCESS_DENIED
                            );
                });

        /*
         * 권한 검증 단계에서 실패
         */
        verify(
                userQueryRepository,
                never()
        ).findByIdAndDeletedAtIsNull(targetUserId);
    }

    /**
     * MASTER라도 관리자 삭제 API로 자기 자신을 삭제할 수 없다.
     */
    @Test
    void MASTER는_자기_자신을_관리자_삭제할_수_없다() {
        // given
        Long masterId = 1L;

        DeleteUserCommandDto command =
                new DeleteUserCommandDto(
                        masterId,
                        UserRole.MASTER,
                        masterId
                );

        // when & then
        assertThatThrownBy(
                () -> userCommandService.deleteUser(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {

                    CustomException customException =
                            (CustomException) exception;

                    assertThat(customException.getErrorCode())
                            .isEqualTo(
                                    UserErrorCode.USER_SELF_DELETE_NOT_ALLOWED
                            );
                });

        verify(
                userQueryRepository,
                never()
        ).findByIdAndDeletedAtIsNull(masterId);
    }

    /**
     * 존재하지 않거나 이미 Soft Delete된 사용자는
     * 활성 사용자 조회에서 발견되지 않는다.
     */
    @Test
    void 존재하지_않거나_삭제된_사용자는_삭제할_수_없다() {
        // given
        Long masterId = 1L;
        Long targetUserId = 999L;

        DeleteUserCommandDto command =
                new DeleteUserCommandDto(
                        masterId,
                        UserRole.MASTER,
                        targetUserId
                );

        when(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(targetUserId)
        ).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> userCommandService.deleteUser(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {

                    CustomException customException =
                            (CustomException) exception;

                    assertThat(customException.getErrorCode())
                            .isEqualTo(
                                    UserErrorCode.USER_NOT_FOUND
                            );
                });
    }
}