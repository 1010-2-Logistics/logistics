package com.logistics.user.application.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.user.application.dto.command.CreateUserCommand;
import com.logistics.user.application.event.UserCreatedEvent;
import com.logistics.user.application.port.EventPublisher;
import com.logistics.user.application.service.UserCommandService;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.repository.UserCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// 참고: User.UserId는 @GeneratedValue라 실제 DB insert 전까진 null입니다.
// 그래서 이 순수 Mockito 단위테스트에서는 생성된 ID 값 자체를 검증하지 않고,
// save/publish가 올바른 인자로 호출됐는지만 검증합니다.
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserCommandRepository UserCommandRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UserCommandService UserCommandService;

    @Test
    void 생성하면_저장하고_이벤트를_발행한다() {
        // given
        when(UserCommandRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserCommandService.create(new CreateUserCommand("샘플"));

        // then
        verify(UserCommandRepository).save(any(User.class));
        verify(eventPublisher).publish(any(UserCreatedEvent.class));
    }
}
