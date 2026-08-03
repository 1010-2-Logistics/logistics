package com.logistics.template.sample.application.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.template.application.dto.command.CreateInventoryCommand;
import com.logistics.template.application.event.InventoryCreatedEvent;
import com.logistics.template.application.port.EventPublisher;
import com.logistics.template.application.service.InventoryCommandService;
import com.logistics.template.domain.entity.Inventory;
import com.logistics.template.domain.repository.InventoryCommandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// 참고: Sample.sampleId는 @GeneratedValue라 실제 DB insert 전까진 null입니다.
// 그래서 이 순수 Mockito 단위테스트에서는 생성된 ID 값 자체를 검증하지 않고,
// save/publish가 올바른 인자로 호출됐는지만 검증합니다.
@ExtendWith(MockitoExtension.class)
class SampleCommandServiceTest {

    @Mock
    private InventoryCommandRepository sampleCommandRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private InventoryCommandService sampleCommandService;

    @Test
    void 생성하면_저장하고_이벤트를_발행한다() {
        // given
        when(sampleCommandRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        sampleCommandService.create(new CreateInventoryCommand("샘플"));

        // then
        verify(sampleCommandRepository).save(any(Inventory.class));
        verify(eventPublisher).publish(any(InventoryCreatedEvent.class));
    }
}
