package com.logistics.template.sample.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.logistics.template.application.dto.query.GetInventoryQuery;
import com.logistics.template.application.service.InventoryQueryService;
import com.logistics.template.domain.entity.Inventory;
import com.logistics.template.domain.repository.InventoryQueryRepository;
import com.logistics.template.global.exception.CustomException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SampleQueryServiceTest {

    @Mock
    private InventoryQueryRepository sampleQueryRepository;

    @InjectMocks
    private InventoryQueryService sampleQueryService;

    @Test
    void 존재하지_않으면_예외를_던진다() {
        // given
        UUID sampleId = UUID.randomUUID();
        when(sampleQueryRepository.findByIdAndDeletedAtIsNull(sampleId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sampleQueryService.get(new GetInventoryQuery(sampleId)))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 존재하면_조회된다() {
        // given
        // Sample.sampleId는 @GeneratedValue라 실제 DB insert 전까진 null이라, 조회 키는 별도 UUID로 지정합니다.
        UUID sampleId = UUID.randomUUID();
        Inventory sample = Inventory.create("샘플");
        when(sampleQueryRepository.findByIdAndDeletedAtIsNull(sampleId)).thenReturn(Optional.of(sample));

        // when
        Inventory result = sampleQueryService.get(new GetInventoryQuery(sampleId));

        // then
        assertThat(result.getName()).isEqualTo("샘플");
    }
}
