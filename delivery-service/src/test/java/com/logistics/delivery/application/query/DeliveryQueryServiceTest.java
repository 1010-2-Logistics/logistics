package com.logistics.delivery.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.logistics.delivery.application.dto.query.SearchDeliveryQuery;
import com.logistics.delivery.application.dto.result.DeliveryResults;
import com.logistics.delivery.application.service.DeliveryQueryService;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
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

    @InjectMocks
    private DeliveryQueryService deliveryQueryService;

    @Test
    void 존재하는_배송을_조회하면_반환한다() {
        // given
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));

        // when
        DeliveryResults.DeliveryDetailResult result = deliveryQueryService.getById(deliveryId);

        // then
        assertThat(result.delivery()).isEqualTo(delivery);
    }

    @Test
    void 존재하지_않는_배송을_조회하면_예외를_던진다() {
        // given
        UUID deliveryId = UUID.randomUUID();
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryQueryService.getById(deliveryId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_NOT_FOUND);
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
}