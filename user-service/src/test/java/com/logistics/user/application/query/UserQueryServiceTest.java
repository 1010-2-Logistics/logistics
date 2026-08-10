package com.logistics.user.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.logistics.user.application.dto.query.GetUserQueryDto;
import com.logistics.user.application.service.UserQueryService;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserQueryRepository userQueryRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    @Test
    void 존재하지_않으면_예외() {
        Long userId = 1L;

        when(userQueryRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> userQueryService.get(new GetUserQueryDto(userId))
        ).isInstanceOf(CustomException.class);
    }

    @Test
    void 존재하면_조회된다() {
        // given
        Long userId = 1L;

        /*
         * MASTER는 companyId와 hubId가 필요하지 않으므로
         * 테스트 데이터를 가장 단순하게 만들 수 있다.
         *
         * encodedPassword에는 실제 BCrypt 값이 아니라
         * 도메인 생성 테스트를 위한 임시 문자열을 전달한다.
         */
        User user = User.create(
                "sample01",
                "encoded-password",
                "U0123456789",
                UserRole.MASTER,
                null,
                null
        );

        when(userQueryRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        // when
        User result = userQueryService.get(
                new GetUserQueryDto(userId)
        );

        // then
        assertThat(result.getUsername())
                .isEqualTo("sample01");

        assertThat(result.getStatus())
                .isEqualTo(UserStatus.PENDING);

        assertThat(result.getRole())
                .isEqualTo(UserRole.MASTER);
    }
}