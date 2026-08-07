package com.logistics.hubRoute.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.logistics.hubRoute.application.dto.query.GetHubRouteQuery;
import com.logistics.hubRoute.application.service.HubRouteQueryService;
import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.domain.repository.HubRouteQueryRepository; // 1. QueryRepository import
import com.logistics.hubRoute.global.exception.CustomException;
import com.logistics.hubRoute.infrastructure.feign.client.HubClient;
import com.logistics.hubRoute.presentation.dto.dto.request.HubRouteFindRequestDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteFindResponseDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class HubRouteQueryServiceTest {

    @Mock
    private HubRouteQueryRepository hubRouteQueryRepository; // 2. Command를 Query로 변경! (핵심)

    @Mock
    private HubClient hubClient;

    @InjectMocks
    private HubRouteQueryService hubRouteQueryService;

    @Test
    void 출발허브와_도착허브가_같으면_예외를_던진다() {
        // given
        UUID sameHubId = UUID.randomUUID();
        HubRouteFindRequestDto requestDto = new HubRouteFindRequestDto(sameHubId, sameHubId);

        // when & then
        assertThatThrownBy(() -> hubRouteQueryService.findHubRoute(requestDto))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 존재하지_않는_허브로_요청하면_예외를_던진다() {
        // given
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        HubRouteFindRequestDto requestDto = new HubRouteFindRequestDto(startHubId, endHubId);

        // Feign Client에서 출발 허브만 검증 성공하도록 반환
        when(hubClient.validateHubIds(anyList())).thenReturn(Set.of(startHubId));

        // when & then
        assertThatThrownBy(() -> hubRouteQueryService.findHubRoute(requestDto))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 단일_직통_경로가_존재하면_해당_경로를_반환한다() {
        // given
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        HubRouteFindRequestDto requestDto = new HubRouteFindRequestDto(startHubId, endHubId);
        HubRoute directRoute = HubRoute.create(startHubId, endHubId, 60, BigDecimal.valueOf(50.0), 1L);

        when(hubClient.validateHubIds(anyList())).thenReturn(Set.of(startHubId, endHubId));
        when(hubRouteQueryRepository.findByStartHubIdAndEndHubIdAndDeletedAtIsNull(startHubId, endHubId))
                .thenReturn(Optional.of(directRoute));

        // when
        HubRouteFindResponseDto result = hubRouteQueryService.findHubRoute(requestDto);

        // then
        assertThat(result.startHubId()).isEqualTo(startHubId);
        assertThat(result.endHubId()).isEqualTo(endHubId);
        assertThat(result.totalDuration()).isEqualTo(60);
        assertThat(result.totalDistance()).isEqualTo(BigDecimal.valueOf(50.0));
        assertThat(result.steps()).hasSize(1);
    }

    @Test
    void 직통_경로가_없을때_다익스트라로_최단_경로를_계산한다() {
        // given (A -> B -> C 경로 세팅)
        UUID hubA = UUID.randomUUID();
        UUID hubB = UUID.randomUUID();
        UUID hubC = UUID.randomUUID();

        HubRouteFindRequestDto requestDto = new HubRouteFindRequestDto(hubA, hubC);

        HubRoute routeAB = HubRoute.create(hubA, hubB, 30, BigDecimal.valueOf(20.0), 1L);
        HubRoute routeBC = HubRoute.create(hubB, hubC, 40, BigDecimal.valueOf(30.0), 1L);

        when(hubClient.validateHubIds(anyList())).thenReturn(Set.of(hubA, hubC));
        when(hubRouteQueryRepository.findByStartHubIdAndEndHubIdAndDeletedAtIsNull(hubA, hubC))
                .thenReturn(Optional.empty()); // 직통 경로 없음
        when(hubRouteQueryRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(routeAB, routeBC)); // 전체 그래프 간선 제공

        // when
        HubRouteFindResponseDto result = hubRouteQueryService.findHubRoute(requestDto);

        // then
        assertThat(result.startHubId()).isEqualTo(hubA);
        assertThat(result.endHubId()).isEqualTo(hubC);
        assertThat(result.totalDuration()).isEqualTo(70); // 30 + 40
        assertThat(result.totalDistance()).isEqualTo(BigDecimal.valueOf(50.0)); // 20.0 + 30.0
        assertThat(result.steps()).hasSize(2);
    }
}