package com.logistics.delivery.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.delivery.application.dto.command.CreateDeliveryCommand;
import com.logistics.delivery.application.service.DeliveryCommandService;
import com.logistics.delivery.application.service.DeliveryManagerAssignmentService;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryRoute;
import com.logistics.delivery.domain.entity.DeliveryRouteStatus;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryCommandServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryRouteRepository deliveryRouteRepository;

    @Mock
    private DeliveryManagerAssignmentService deliveryManagerAssignmentService;

    @InjectMocks
    private DeliveryCommandService deliveryCommandService;

    @Test
    void 배송이_정상적으로_생성된다() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, startHubId, endHubId, "서울시 송파구", "홍길동", "U01");

        when(deliveryRepository.save(any(Delivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryManager manager = DeliveryManager.create(1L, null, "M01", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(manager);
        when(deliveryRouteRepository.save(any(DeliveryRoute.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Delivery result = deliveryCommandService.create(command);

        // then
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getStartHubId()).isEqualTo(startHubId);
        assertThat(result.getEndHubId()).isEqualTo(endHubId);
        assertThat(result.getDeliveryAddress()).isEqualTo("서울시 송파구");
        assertThat(result.getReceiverName()).isEqualTo("홍길동");
        assertThat(result.getSlackId()).isEqualTo("U01");
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.HUB_WAITING);
        assertThat(result.getCreatedBy()).isNotNull();
    }

    @Test
    void 배송_생성시_배송_경로도_함께_생성되고_담당자가_배정된다() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, startHubId, endHubId, "서울시 송파구", "홍길동", "U01");

        when(deliveryRepository.save(any(Delivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryManager manager = DeliveryManager.create(7L, null, "M07", ManagerType.HUB_DELIVERY_MANAGER, 2);
        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(manager);

        ArgumentCaptor<DeliveryRoute> routeCaptor = ArgumentCaptor.forClass(DeliveryRoute.class);
        when(deliveryRouteRepository.save(routeCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        deliveryCommandService.create(command);

        // then
        DeliveryRoute savedRoute = routeCaptor.getValue();
        assertThat(savedRoute.getSequence()).isEqualTo(0);
        assertThat(savedRoute.getStartHubId()).isEqualTo(startHubId);
        assertThat(savedRoute.getEndHubId()).isEqualTo(endHubId);
        assertThat(savedRoute.getDeliveryManagerId()).isEqualTo(7L);
        assertThat(savedRoute.getStatus()).isEqualTo(DeliveryRouteStatus.HUB_MOVE_WAITING);
        assertThat(savedRoute.getExpectedDistance()).isGreaterThan(BigDecimal.ZERO);
        assertThat(savedRoute.getCreatedBy()).isNotNull();
    }

    @Test
    void 배정_가능한_담당자가_없으면_예외가_전파되고_배송_경로는_저장되지_않는다() {
        // given
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01");

        when(deliveryRepository.save(any(Delivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenThrow(new CustomException(DeliveryErrorCode.DELIVERY_NO_AVAILABLE_MANAGER));

        // when & then
        assertThatThrownBy(() -> deliveryCommandService.create(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_NO_AVAILABLE_MANAGER);

        verify(deliveryRouteRepository, never()).save(any());
    }
}