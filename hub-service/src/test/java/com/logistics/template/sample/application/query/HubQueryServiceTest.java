package com.logistics.template.sample.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.logistics.hub.application.dto.query.GetHubQuery;
import com.logistics.hub.application.service.HubQueryService;
import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.domain.repository.HubQueryRepository;
import com.logistics.hub.global.exception.CustomException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HubQueryServiceTest {

    @Mock
    private HubQueryRepository hubQueryRepository;

    @InjectMocks
    private HubQueryService hubQueryService;

    @Test
    void 존재하지_않으면_예외를_던진다() {
        // given
        UUID sampleId = UUID.randomUUID();
        when(hubQueryRepository.findByIdAndDeletedAtIsNull(sampleId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubQueryService.get(new GetHubQuery(sampleId)))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 존재하면_조회된다() {
        // given
        // Sample.sampleId는 @GeneratedValue라 실제 DB insert 전까진 null이라, 조회 키는 별도 UUID로 지정합니다.
        UUID sampleId = UUID.randomUUID();
        Hub hub = Hub.create("샘플",
                "주소",
                BigDecimal.valueOf(37.1234567),
                BigDecimal.valueOf(127.1234567),

                request.getCreatedBy());
        when(hubQueryRepository.findByIdAndDeletedAtIsNull(sampleId)).thenReturn(Optional.of(hub));

        // when
        Hub result = hubQueryService.get(new GetHubQuery(sampleId));

        // then
        assertThat(result.getHubName()).isEqualTo("샘플");
    }
}
