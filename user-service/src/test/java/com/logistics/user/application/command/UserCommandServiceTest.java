package com.logistics.user.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.user.application.dto.command.CreateUserCommandDto;
import com.logistics.user.application.event.UserCreatedEvent;
import com.logistics.user.application.port.EventPublisher;
import com.logistics.user.application.service.UserCommandService;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.domain.repository.UserCommandRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserCommandService의 사용자 생성 흐름을 검증하는 단위 테스트다.
 *
 * 실제 DB는 사용하지 않고 Repository와 EventPublisher를 Mock으로 대체한다.
 */
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserCommandRepository userCommandRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    void 생성하면_저장하고_이벤트를_발행한다() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();

        CreateUserCommandDto command = new CreateUserCommandDto(
                "sample01",
                "encoded-password",
                "U0123456789",
                UserRole.COMPANY_MANAGER,
                companyId,
                hubId
        );

        when(userCommandRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Long result = userCommandService.create(command);

        // then
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userCommandRepository).save(userCaptor.capture());
        verify(eventPublisher).publish(any(UserCreatedEvent.class));

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername())
                .isEqualTo("sample01");

        assertThat(savedUser.getPassword())
                .isEqualTo("encoded-password");

        assertThat(savedUser.getSlackId())
                .isEqualTo("U0123456789");

        assertThat(savedUser.getRole())
                .isEqualTo(UserRole.COMPANY_MANAGER);

        assertThat(savedUser.getStatus())
                .isEqualTo(UserStatus.PENDING);

        assertThat(savedUser.getCompanyId())
                .isEqualTo(companyId);

        assertThat(savedUser.getHubId())
                .isEqualTo(hubId);

        /*
         * Mockito 테스트에서는 GeneratedValue가 동작하지 않으므로
         * 저장 후 userId는 null이다.
         */
        assertThat(result).isNull();
    }
}