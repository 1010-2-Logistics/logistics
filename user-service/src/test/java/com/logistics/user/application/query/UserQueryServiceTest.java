package com.logistics.user.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.logistics.user.application.dto.query.GetUserQuery;
import com.logistics.user.application.service.UserQueryService;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.exception.CustomException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserQueryRepository UserQueryRepository;

    @InjectMocks
    private UserQueryService UserQueryService;

    @Test
    void 존재하지_않으면_예외를_던진다() {
        // given
        UUID UserId = UUID.randomUUID();
        when(UserQueryRepository.findByIdAndDeletedAtIsNull(UserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> UserQueryService.get(new GetUserQuery(UserId)))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 존재하면_조회된다() {
        // given
        // User.UserId는 @GeneratedValue라 실제 DB insert 전까진 null이라, 조회 키는 별도 UUID로 지정합니다.
        UUID UserId = UUID.randomUUID();
        User User = User.create("샘플");
        when(UserQueryRepository.findByIdAndDeletedAtIsNull(UserId)).thenReturn(Optional.of(User));

        // when
        User result = UserQueryService.get(new GetUserQuery(UserId));

        // then
        assertThat(result.getName()).isEqualTo("샘플");
    }
}
