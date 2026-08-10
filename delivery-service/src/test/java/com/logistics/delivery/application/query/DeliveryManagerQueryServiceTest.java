package com.logistics.delivery.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.logistics.delivery.application.dto.query.SearchDeliveryManagerQuery;
import com.logistics.delivery.application.service.DeliveryManagerQueryService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
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
class DeliveryManagerQueryServiceTest {

    @Mock
    private DeliveryManagerRepository deliveryManagerRepository;

    @InjectMocks
    private DeliveryManagerQueryService deliveryManagerQueryService;

    private static final UserPrincipal MASTER = new UserPrincipal(1L, Role.MASTER, null, null);

    @Test
    void 존재하는_담당자를_조회하면_반환한다() {
        // given
        DeliveryManager manager = DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(manager));

        // when
        DeliveryManager result = deliveryManagerQueryService.getById(1L, MASTER);

        // then
        assertThat(result.getDeliveryManagerId()).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_담당자를_조회하면_예외를_던진다() {
        // given
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryManagerQueryService.getById(999L, MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND);
    }

    @Test
    void 본인이_아닌_배송담당자_조회시_예외() {
        // given
        DeliveryManager manager = DeliveryManager.create(2L, null, "U02", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(manager));
        UserPrincipal other = new UserPrincipal(99L, Role.HUB_DELIVERY_MANAGER, null, null);

        // when & then
        assertThatThrownBy(() -> deliveryManagerQueryService.getById(2L, other))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }

    @Test
    void 담당_허브가_아닌_HUB_MANAGER는_조회할_수_없다() {
        // given
        UUID managerHubId = UUID.randomUUID();
        DeliveryManager manager = DeliveryManager.create(3L, managerHubId, "U03", ManagerType.COMPANY_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.of(manager));
        UserPrincipal otherHubManager = new UserPrincipal(50L, Role.HUB_MANAGER, UUID.randomUUID(), null);

        // when & then
        assertThatThrownBy(() -> deliveryManagerQueryService.getById(3L, otherHubManager))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }

    @Test
    void 목록_조회시_size가_허용된_값이_아니면_10으로_보정된다() {
        // given
        SearchDeliveryManagerQuery query = SearchDeliveryManagerQuery.of(null, null, 0, 999);

        // then
        assertThat(query.size()).isEqualTo(10);
    }

    @Test
    void 목록_조회시_필터_조건대로_조회한다() {
        // given
        DeliveryManager manager = DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0);
        Page<DeliveryManager> page = new PageImpl<>(List.of(manager), PageRequest.of(0, 10), 1);
        when(deliveryManagerRepository.search(any(), any(), any())).thenReturn(page);

        SearchDeliveryManagerQuery query = SearchDeliveryManagerQuery.of(ManagerType.HUB_DELIVERY_MANAGER, null, 0, 10);

        // when
        Page<DeliveryManager> result = deliveryManagerQueryService.search(query, MASTER);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void HUB_MANAGER가_목록_조회하면_본인_허브로_강제_스코프된다() {
        // given
        UUID hubId = UUID.randomUUID();
        Page<DeliveryManager> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(deliveryManagerRepository.search(any(), any(), any())).thenReturn(page);
        UserPrincipal hubManager = new UserPrincipal(10L, Role.HUB_MANAGER, hubId, null);

        SearchDeliveryManagerQuery query = SearchDeliveryManagerQuery.of(null, UUID.randomUUID(), 0, 10);

        // when
        deliveryManagerQueryService.search(query, hubManager);

        // then: 쿼리에 넘긴 hubId가 아니라 principal의 hubId로 조회됐는지 확인
        org.mockito.Mockito.verify(deliveryManagerRepository)
                .search(null, hubId, PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
    }

    @Test
    void 배송담당자가_목록_조회시도하면_예외() {
        UserPrincipal deliveryManager = new UserPrincipal(20L, Role.HUB_DELIVERY_MANAGER, null, null);
        SearchDeliveryManagerQuery query = SearchDeliveryManagerQuery.of(null, null, 0, 10);

        assertThatThrownBy(() -> deliveryManagerQueryService.search(query, deliveryManager))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }
}
