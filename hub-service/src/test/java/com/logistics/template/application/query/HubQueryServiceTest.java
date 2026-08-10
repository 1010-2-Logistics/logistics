package com.logistics.template.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.logistics.hub.application.dto.query.GetHubQuery;
import com.logistics.hub.application.service.HubQueryService;
import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.domain.repository.HubQueryRepository;
import com.logistics.hub.global.exception.CustomException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.logistics.hub.presentation.dto.dto.response.HubResponseDto;
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
                1L);
        when(hubQueryRepository.findByIdAndDeletedAtIsNull(sampleId)).thenReturn(Optional.of(hub));

        // when
        Hub result = hubQueryService.get(new GetHubQuery(sampleId));

        // then
        assertThat(result.getHubName()).isEqualTo("샘플");
    }

    //허브 내부 api 테스트 -> 허브 정보 리스트
    @Test
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

        // ==================== [ 리턴 값 콘솔 출력 ] ====================
        System.out.println("========== [ 결과 값 확인 ] ==========");
        result.forEach(dto ->
                System.out.println("Hub ID: " + dto.hubId() +
                        ", 이름: " + dto.name() +
                        ", 주소: " + dto.hubAddress())
        );
        System.out.println("======================================");
        // ==============================================================

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(HubResponseDto::name)
                .containsExactlyInAnyOrder("서울 허브", "경기 허브");
    }

    @Test
    void 요청한_허브_ID_중_존재하지_않거나_삭제된_허브가_있으면_예외를_던진다() {
        // given
        UUID hubId1 = UUID.randomUUID();
        UUID hubId2 = UUID.randomUUID(); // DB에 없는 ID
        List<UUID> hubIds = List.of(hubId1, hubId2);

        Hub hub1 = Hub.create("서울 허브", "서울시 강남구", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), 1L);

        // DB에서 2개 중 1개만 조회되는 상황 모킹
        when(hubQueryRepository.findAllByHubIdInAndDeletedAtIsNull(hubIds))
                .thenReturn(List.of(hub1));

        // when & then
        assertThatThrownBy(() -> hubQueryService.getHubsInternal(hubIds))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void hubIds가_null이거나_비어있으면_예외를_던진다() {
        // given
        List<UUID> emptyHubIds = List.of();

        // when & then
        assertThatThrownBy(() -> hubQueryService.getHubsInternal(emptyHubIds))
                .isInstanceOf(CustomException.class);
    }

}
