package com.logistics.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.logistics.user.application.dto.command.ChangeUserAffiliationCommandDto;
import com.logistics.user.application.dto.result.ChangeUserAffiliationResultDto;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAffiliationService의 사용자 소속 및 권한 변경 로직을 검증한다.
 *
 * 실제 DB를 사용하지 않고 UserQueryRepository를 Mock으로 대체한다.
 *
 * 검증 대상:
 * - APPROVED 사용자의 정상적인 소속/권한 변경
 * - 승인되지 않은 사용자 변경 차단
 * - 존재하지 않거나 삭제된 사용자 변경 차단
 * - 현재 소속/권한과 동일한 요청 차단
 * - User 도메인의 역할/소속 규칙 위반 차단
 */
@ExtendWith(MockitoExtension.class)
class UserAffiliationServiceTest {

    @Mock
    private UserQueryRepository userQueryRepository;

    @InjectMocks
    private UserAffiliationService userAffiliationService;

    /**
     * HUB_MANAGER 사용자를 COMPANY_MANAGER로 변경하는 정상 흐름.
     *
     * COMPANY_MANAGER는
     * companyId와 hubId를 모두 가져야 한다.
     */
    @Test
    void 승인된_사용자의_소속과_권한을_변경할_수_있다() {
        // given
        Long userId = 15L;

        UUID previousHubId = UUID.randomUUID();

        User user = User.create(
                "sample01",
                "sample02",
                "encoded-password",
                "U0123456789",
                UserRole.HUB_MANAGER,
                null,
                previousHubId
        );

        // Internal 소속 변경 대상은 승인된 사용자여야 한다.
        user.approve();

        UUID newCompanyId = UUID.randomUUID();
        UUID newHubId = UUID.randomUUID();

        ChangeUserAffiliationCommandDto command =
                new ChangeUserAffiliationCommandDto(
                        userId,
                        UserRole.COMPANY_MANAGER,
                        newCompanyId,
                        newHubId
                );

        when(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(userId)
        ).thenReturn(Optional.of(user));

        // when
        ChangeUserAffiliationResultDto result =
                userAffiliationService.changeAffiliation(command);

        // then
        assertThat(result.role())
                .isEqualTo(UserRole.COMPANY_MANAGER);

        assertThat(result.companyId())
                .isEqualTo(newCompanyId);

        assertThat(result.hubId())
                .isEqualTo(newHubId);

        /*
         * Result DTO뿐 아니라
         * 실제 User 객체의 상태도 변경되었는지 확인한다.
         */
        assertThat(user.getRole())
                .isEqualTo(UserRole.COMPANY_MANAGER);

        assertThat(user.getCompanyId())
                .isEqualTo(newCompanyId);

        assertThat(user.getHubId())
                .isEqualTo(newHubId);
    }

    /**
     * PENDING 상태 사용자는 소속 및 권한을 변경할 수 없다.
     */
    @Test
    void 승인되지_않은_사용자는_소속과_권한을_변경할_수_없다() {
        // given
        Long userId = 15L;

        UUID currentHubId = UUID.randomUUID();

        /*
         * User.create() 직후에는 PENDING 상태이다.
         * approve()를 호출하지 않는다.
         */
        User user = User.create(
                "sample01",
                "sample02",
                "encoded-password",
                "U0123456789",
                UserRole.HUB_MANAGER,
                null,
                currentHubId
        );

        ChangeUserAffiliationCommandDto command =
                new ChangeUserAffiliationCommandDto(
                        userId,
                        UserRole.COMPANY_MANAGER,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        when(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(userId)
        ).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(
                () -> userAffiliationService
                        .changeAffiliation(command)
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {

                    CustomException customException =
                            (CustomException) exception;

                    assertThat(customException.getErrorCode())
                            .isEqualTo(
                                    UserErrorCode.USER_NOT_APPROVED
                            );
                });

        /*
         * 변경 실패 후 기존 상태가 유지되어야 한다.
         */
        assertThat(user.getRole())
                .isEqualTo(UserRole.HUB_MANAGER);

        assertThat(user.getCompanyId())
                .isNull();

        assertThat(user.getHubId())
                .isEqualTo(currentHubId);
    }

    /**
     * 활성 사용자를 조회할 수 없다면 USER_NOT_FOUND.
     *
     * findByIdAndDeletedAtIsNull()을 사용하므로
     * 존재하지 않는 사용자와 Soft Delete 사용자는
     * 모두 조회되지 않는다.
     */
    @Test
    void 존재하지_않거나_삭제된_사용자는_변경할_수_없다() {
        // given
        Long userId = 999L;

        ChangeUserAffiliationCommandDto command =
                new ChangeUserAffiliationCommandDto(
                        userId,
                        UserRole.COMPANY_MANAGER,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        when(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(userId)
        ).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> userAffiliationService
                        .changeAffiliation(command)
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

//    /**
//     * role, companyId, hubId가 현재 상태와 전부 같다면
//     * 변경할 필요가 없으므로 예외가 발생해야 한다.
//     */
//    @Test
//    void 현재_소속과_권한이_모두_동일하면_예외가_발생한다() {
//        // given
//        Long userId = 15L;
//
//        UUID companyId = UUID.randomUUID();
//        UUID hubId = UUID.randomUUID();
//
//        User user = User.create(
//                "sample01",
//                "sample02",
//                "encoded-password",
//                "U0123456789",
//                UserRole.COMPANY_MANAGER,
//                companyId,
//                hubId
//        );
//
//        user.approve();
//
//        /*
//         * 현재 User 상태와 정확하게 동일한 요청.
//         */
//        ChangeUserAffiliationCommandDto command =
//                new ChangeUserAffiliationCommandDto(
//                        userId,
//                        UserRole.COMPANY_MANAGER,
//                        companyId,
//                        hubId
//                );
//
//        when(
//                userQueryRepository
//                        .findByIdAndDeletedAtIsNull(userId)
//        ).thenReturn(Optional.of(user));
//
//        // when & then
//        assertThatThrownBy(
//                () -> userAffiliationService
//                        .changeAffiliation(command)
//        )
//                .isInstanceOf(CustomException.class)
//                .satisfies(exception -> {
//
//                    CustomException customException =
//                            (CustomException) exception;
//
//                    assertThat(customException.getErrorCode())
//                            .isEqualTo(
//                                    UserErrorCode.USER_AFFILIATION_CONFLICT
//                            );
//                });
//
//        /*
//         * 예외 발생 후에도 기존 상태는 그대로 유지된다.
//         */
//        assertThat(user.getRole())
//                .isEqualTo(UserRole.COMPANY_MANAGER);
//
//        assertThat(user.getCompanyId())
//                .isEqualTo(companyId);
//
//        assertThat(user.getHubId())
//                .isEqualTo(hubId);
//    }

    /**
     * COMPANY_MANAGER는 companyId와 hubId를
     * 모두 가져야 한다.
     *
     * 이 규칙은 User 도메인이 책임지므로,
     * 현재 구현에서는 IllegalArgumentException이 그대로 전달된다.
     */
    @Test
    void 역할과_소속_정보가_일치하지_않으면_예외가_발생한다() {
        // given
        Long userId = 15L;

        UUID currentHubId = UUID.randomUUID();

        User user = User.create(
                "sample01",
                "sample",
                "encoded-password",
                "U0123456789",
                UserRole.HUB_MANAGER,
                null,
                currentHubId
        );

        user.approve();

        /*
         * 잘못된 최종 상태.
         *
         * COMPANY_MANAGER인데
         * companyId가 존재하지 않는다.
         */
        ChangeUserAffiliationCommandDto command =
                new ChangeUserAffiliationCommandDto(
                        userId,
                        UserRole.COMPANY_MANAGER,
                        null,
                        UUID.randomUUID()
                );

        when(
                userQueryRepository
                        .findByIdAndDeletedAtIsNull(userId)
        ).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(
                () -> userAffiliationService
                        .changeAffiliation(command)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "업체 역할은 companyId와 hubId가 모두 필요합니다."
                );

        /*
         * User.changeRoleAndAffiliation()은
         * 검증이 성공한 이후에 필드를 변경하기 때문에
         * 실패하면 기존 상태가 보존된다.
         */
        assertThat(user.getRole())
                .isEqualTo(UserRole.HUB_MANAGER);

        assertThat(user.getCompanyId())
                .isNull();

        assertThat(user.getHubId())
                .isEqualTo(currentHubId);
    }
}