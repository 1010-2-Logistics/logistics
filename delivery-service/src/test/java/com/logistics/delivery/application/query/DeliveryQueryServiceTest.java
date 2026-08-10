package com.logistics.delivery.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.logistics.delivery.application.dto.query.SearchDeliveryQuery;
import com.logistics.delivery.application.dto.result.DeliveryResults;
import com.logistics.delivery.application.service.DeliveryQueryService;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryRoute;
import com.logistics.delivery.domain.entity.DeliveryRouteStatus;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DeliveryQueryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryRouteRepository deliveryRouteRepository;

    @InjectMocks
    private DeliveryQueryService deliveryQueryService;

    private static final UserPrincipal MASTER = new UserPrincipal(1L, Role.MASTER, null, null);

    @Test
    void 존재하는_배송을_조회하면_반환한다() {
        // given
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRouteRepository.findAllByDeliveryId(deliveryId)).thenReturn(List.of());

        // when
        DeliveryResults.DeliveryDetailResult result = deliveryQueryService.getById(deliveryId, MASTER);

        // then
        assertThat(result.delivery()).isEqualTo(delivery);
    }

    @Test
    void 존재하지_않는_배송을_조회하면_예외를_던진다() {
        // given
        UUID deliveryId = UUID.randomUUID();
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryQueryService.getById(deliveryId, MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }

    @Test
    void 담당_허브가_아닌_HUB_MANAGER는_조회할_수_없다() {
        // given
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRouteRepository.findAllByDeliveryId(deliveryId)).thenReturn(List.of());
        UserPrincipal otherHubManager = new UserPrincipal(5L, Role.HUB_MANAGER, UUID.randomUUID(), null);

        // when & then
        assertThatThrownBy(() -> deliveryQueryService.getById(deliveryId, otherHubManager))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }

    @Test
    void 담당_허브인_HUB_MANAGER는_조회할_수_있다() {
        // given
        UUID deliveryId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), startHubId, UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRouteRepository.findAllByDeliveryId(deliveryId)).thenReturn(List.of());
        UserPrincipal hubManager = new UserPrincipal(5L, Role.HUB_MANAGER, startHubId, null);

        // when
        DeliveryResults.DeliveryDetailResult result = deliveryQueryService.getById(deliveryId, hubManager);

        // then
        assertThat(result.delivery()).isEqualTo(delivery);
    }

    @Test
    void 목록_조회시_size가_허용된_값이_아니면_10으로_보정된다() {
        // given
        SearchDeliveryQuery query = SearchDeliveryQuery.of(null, null, null, 0, 999);

        // then
        assertThat(query.size()).isEqualTo(10);
    }

    @Test
    void 목록_조회시_sort가_없으면_createdAt으로_보정된다() {
        // given
        SearchDeliveryQuery query = SearchDeliveryQuery.of(null, null, null, 0, 10);

        // then
        assertThat(query.sort()).isEqualTo("createdAt");
    }

    @Test
    void 목록_조회시_필터_조건대로_조회한다() {
        // given
        UUID hubId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), hubId, UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        Page<Delivery> page = new PageImpl<>(List.of(delivery), PageRequest.of(0, 10), 1);
        when(deliveryRepository.search(any(), any(), any())).thenReturn(page);

        SearchDeliveryQuery query = SearchDeliveryQuery.of(DeliveryStatus.HUB_WAITING, hubId, "createdAt", 0, 10);

        // when
        Page<DeliveryResults.DeliveryDetailResult> result = deliveryQueryService.search(query);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void 배송_경로_목록을_순번대로_조회한다() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));

        DeliveryRoute route = DeliveryRoute.create(deliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);
        when(deliveryRouteRepository.findAllByDeliveryId(deliveryId)).thenReturn(List.of(route));

        DeliveryResults.DeliveryRouteListResult result = deliveryQueryService.getRoutes(deliveryId, MASTER);

        assertThat(result.routes()).hasSize(1);
        assertThat(result.routes().get(0).getSequence()).isEqualTo(0);
    }

    @Test
    void 존재하지_않는_배송의_경로_조회시_예외() {
        UUID deliveryId = UUID.randomUUID();
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryQueryService.getRoutes(deliveryId, MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }

    @Test
    void 내부_경로_조회는_소유권_검증_없이_조회된다() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));

        DeliveryRoute route = DeliveryRoute.create(deliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);
        when(deliveryRouteRepository.findAllByDeliveryId(deliveryId)).thenReturn(List.of(route));

        DeliveryResults.DeliveryRouteListResult result = deliveryQueryService.getRoutesInternal(deliveryId);

        assertThat(result.routes()).hasSize(1);
    }

    @Test
    void 경로_진행중이면_현재_구간_담당자를_반환한다() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        delivery.changeStatus(DeliveryStatus.HUB_MOVING);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));

        DeliveryRoute route1 = DeliveryRoute.create(deliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);
        route1.changeStatus(DeliveryRouteStatus.DEST_HUB_ARRIVED);
        DeliveryRoute route2 = DeliveryRoute.create(deliveryId, 1, UUID.randomUUID(), UUID.randomUUID(), 2L, BigDecimal.TEN, 30, 1L);
        route2.changeStatus(DeliveryRouteStatus.HUB_MOVING);
        when(deliveryRouteRepository.findAllByDeliveryId(deliveryId)).thenReturn(List.of(route1, route2));

        DeliveryResults.DeliveryInternalResult result = deliveryQueryService.getInternal(deliveryId);

        assertThat(result.currentManagerId()).isEqualTo(2L);
    }

    @Test
    void 업체이동중이면_업체담당자를_현재담당자로_반환한다() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        delivery.changeStatus(DeliveryStatus.COMPANY_MOVING);
        delivery.assignCompanyDeliveryManager(9L);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRouteRepository.findAllByDeliveryId(deliveryId)).thenReturn(List.of());

        DeliveryResults.DeliveryInternalResult result = deliveryQueryService.getInternal(deliveryId);

        assertThat(result.currentManagerId()).isEqualTo(9L);
    }

    @Test
    void 취소된_배송은_현재담당자가_없다() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        delivery.changeStatus(DeliveryStatus.CANCELLED);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRouteRepository.findAllByDeliveryId(deliveryId)).thenReturn(List.of());

        DeliveryResults.DeliveryInternalResult result = deliveryQueryService.getInternal(deliveryId);

        assertThat(result.currentManagerId()).isNull();
    }
}
