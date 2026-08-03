package com.logistics.order.application.facade;

import java.util.UUID;

import com.logistics.order.application.dto.command.CreateSampleCommand;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.application.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Controller가 바라보는 단일 진입점. 여러 서비스를 조합해야 하는 복잡한 유스케이스에서만 쓰고,
// 단순 CRUD는 Controller가 SampleCommandService/SampleQueryService를 바로 호출해도 됩니다.
@Component
@RequiredArgsConstructor
public class SampleFacade {

    private final OrderCommandService sampleCommandService;
    private final OrderQueryService orderQueryService;

    public UUID createSample() {
        // 예: 생성 전에 다른 서비스(Feign) 검증이 필요하다면 여기서 조합
        return null;
    }
}
