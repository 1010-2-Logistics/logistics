package com.logistics.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.logistics.user.application.dto.result.InternalUserResultDto;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceInternalTest {

    @Mock
    private UserQueryRepository userQueryRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    @Test
    void 내부_사용자_정보를_조회할_수_있다() {
        // given
        Long userId = 5L;

        User user = User.create(
                "test01",
                "김필수",
                "encoded-password",
                "U_TEST_001",
                UserRole.COMPANY_MANAGER,
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID()
        );

        when(
                userQueryRepository.findByIdAndDeletedAtIsNull(userId)
        ).thenReturn(Optional.of(user));

        // when
        InternalUserResultDto result =
                userQueryService.getInternalUser(userId);

        // then
        assertThat(result.name())
                .isEqualTo("김필수");

        assertThat(result.slackId())
                .isEqualTo("U_TEST_001");
    }

    @Test
    void 존재하지_않는_사용자는_조회할_수_없다() {
        // given
        Long userId = 999L;

        when(
                userQueryRepository.findByIdAndDeletedAtIsNull(userId)
        ).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> userQueryService.getInternalUser(userId)
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