package com.logistics.user.application.facade;

import com.logistics.user.application.dto.command.CreateUserCommand;
import com.logistics.user.application.service.UserCommandService;
import com.logistics.user.application.service.UserQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Controller가 바라보는 단일 진입점. 여러 서비스를 조합해야 하는 복잡한 유스케이스에서만 쓰고,
// 단순 CRUD는 Controller가 UserCommandService/UserQueryService를 바로 호출해도 됩니다.
@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserCommandService UserCommandService;
    private final UserQueryService UserQueryService;

    public Long createUser(CreateUserCommand command) {
        // 예: 생성 전에 다른 서비스(Feign) 검증이 필요하다면 여기서 조합
        return UserCommandService.create(command);
    }
}
