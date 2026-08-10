package com.logistics.delivery.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.logistics.delivery.application.dto.query.SearchDeliveryManagerQuery;
import com.logistics.delivery.application.service.DeliveryManagerQueryService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import java.util.List;
import java.util.Optional;
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

    @Test
    void 존재하는_담당자를_조회하면_반환한다() {
        // given
        DeliveryManager manager = DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(manager));

        // when
        DeliveryManager result = deliveryManagerQueryService.getById(1L);

        // then
        assertThat(result.getDeliveryManagerId()).isEqualTo(1L);
    }

    @Test
    void 존재하지_않는_담당자를_조회하면_예외를_던진다() {
        // given
        when(deliveryManagerRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryManagerQueryService.getById(999L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND);
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
        Page<DeliveryManager> result = deliveryManagerQueryService.search(query);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}