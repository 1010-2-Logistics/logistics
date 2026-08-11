package com.logistics.template.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.logistics.hub.application.dto.query.GetHubQuery;
import com.logistics.hub.application.service.HubQueryService;
import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.domain.repository.HubQueryRepository;
import com.logistics.hub.global.exception.CustomException;
import com.logistics.hub.presentation.dto.dto.response.HubResponseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("존재하지 않는 허브 ID 조회 시 예외를 던진다")
    void 존재하지_않으면_예외를_던진다() {
        // given
        UUID sampleId = UUID.randomUUID();
        when(hubQueryRepository.findByIdAndDeletedAtIsNull(sampleId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hubQueryService.get(new GetHubQuery(sampleId)))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("존재하는 허브 ID 조회 시 허브 엔티티를 반환한다")
    void 존재하면_조회된다() {
        // given
        UUID sampleId = UUID.randomUUID();
        Hub hub = Hub.create(
                "샘플",
                "주소",
                BigDecimal.valueOf(37.1234567),
                BigDecimal.valueOf(127.1234567),
                1L
        );
        when(hubQueryRepository.findByIdAndDeletedAtIsNull(sampleId)).thenReturn(Optional.of(hub));

        // when
        Hub result = hubQueryService.get(new GetHubQuery(sampleId));

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("유효한 허브 목록이 모두 존재하면 HubResponseDto Set을 반환한다")
    void 유효한_허브_목록이_모두_존재하면_HubResponseDto_Set을_반환한다() {
        // given
        UUID hubId1 = UUID.randomUUID();
        UUID hubId2 = UUID.randomUUID();
        List<UUID> hubIds = List.of(hubId1, hubId2);

        Hub hub1 = Hub.create("서울 허브", "서울시 강남구", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), 1L);
        Hub hub2 = Hub.create("경기 허브", "경기도 성남시", BigDecimal.valueOf(37.4), BigDecimal.valueOf(127.1), 1L);

        when(hubQueryRepository.findAllByHubIdInAndDeletedAtIsNull(hubIds))
                .thenReturn(List.of(hub1, hub2));

        // when
        Set<HubResponseDto> result = hubQueryService.getHubsInternal(hubIds);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("요청한 허브 ID 중 존재하지 않거나 삭제된 허브가 있으면 예외를 던진다")
    void 요청한_허브_ID_중_존재하지_않거나_삭제된_허브가_있으면_예외를_던진다() {
        // given
        UUID hubId1 = UUID.randomUUID();
        UUID hubId2 = UUID.randomUUID();
        List<UUID> hubIds = List.of(hubId1, hubId2);

        Hub hub1 = Hub.create("서울 허브", "서울시 강남구", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), 1L);

        when(hubQueryRepository.findAllByHubIdInAndDeletedAtIsNull(hubIds))
                .thenReturn(List.of(hub1));

        // when & then
        assertThatThrownBy(() -> hubQueryService.getHubsInternal(hubIds))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("hubIds가 null이거나 비어있으면 예외를 던진다")
    void hubIds가_null이거나_비어있으면_예외를_던진다() {
        // given
        List<UUID> emptyHubIds = List.of();

        // when & then
        assertThatThrownBy(() -> hubQueryService.getHubsInternal(emptyHubIds))
                .isInstanceOf(CustomException.class);
    }
}