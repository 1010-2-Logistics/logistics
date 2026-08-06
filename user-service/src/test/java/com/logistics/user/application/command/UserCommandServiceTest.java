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

        CreateUserCommandDto command = new CreateUserCommandDto(
                "sample01",
                "encoded-password",
                "U0123456789",
                UserRole.COMPANY_MANAGER,
                companyId,
                null
        );

        /*
         * save()가 호출되면 전달받은 User 객체를 그대로 반환하도록 설정한다.
         *
         * 실제 JPA Repository라면 저장 시 userId가 생성되지만,
         * 순수 Mockito 테스트에서는 userId가 null인 상태다.
         */
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
                .isNull();

        /*
         * 실제 DB 저장이 아니므로 GeneratedValue가 동작하지 않는다.
         * 따라서 반환되는 userId도 null이다.
         */
        assertThat(result).isNull();
    }
}