package com.logistics.delivery.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.delivery.application.dto.command.ChangeDeliveryRouteStatusCommand;
import com.logistics.delivery.application.dto.command.ChangeDeliveryStatusCommand;
import com.logistics.delivery.application.dto.command.CreateDeliveryCommand;
import com.logistics.delivery.application.dto.result.DeliveryResults;
import com.logistics.delivery.application.dto.result.DeliveryResults.DeliveryCreateResult;
import com.logistics.delivery.application.dto.result.DeliveryResults.RouteStatusChangeResult;
import com.logistics.delivery.application.port.HubPort;
import com.logistics.delivery.application.port.HubRoutePort;
import com.logistics.delivery.application.service.DeliveryCommandService;
import com.logistics.delivery.application.service.DeliveryManagerAssignmentService;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryRoute;
import com.logistics.delivery.domain.entity.DeliveryRouteStatus;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import feign.FeignException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Mock
    private HubPort hubPort;

    @Mock
    private HubRoutePort hubRoutePort;

    @InjectMocks
    private DeliveryCommandService deliveryCommandService;

    private static final UserPrincipal MASTER = new UserPrincipal(1L, Role.MASTER, null, null);

    private List<HubRoutePort.HubRouteSegment> singleSegment(UUID startHubId, UUID endHubId) {
        return List.of(new HubRoutePort.HubRouteSegment(startHubId, endHubId, BigDecimal.ONE, 1));
    }

    // ===== create =====

    @Test
    void 배송이_정상적으로_생성된다() {
        UUID orderId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, startHubId, endHubId, "서울시 송파구", "홍길동", "U01");

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(hubPort.validateHubIds(List.of(startHubId))).thenReturn(Set.of(startHubId));
        when(hubPort.validateHubIds(List.of(endHubId))).thenReturn(Set.of(endHubId));
        when(hubRoutePort.findRoute(startHubId, endHubId)).thenReturn(singleSegment(startHubId, endHubId));
        when(deliveryRepository.save(any(Delivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryManager manager = DeliveryManager.create(1L, null, "M01", ManagerType.HUB_DELIVERY_MANAGER, 0);
        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(manager);
        when(deliveryRouteRepository.save(any(DeliveryRoute.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryCreateResult result = deliveryCommandService.create(command);

        Delivery delivery = result.delivery();
        assertThat(delivery.getOrderId()).isEqualTo(orderId);
        assertThat(delivery.getStartHubId()).isEqualTo(startHubId);
        assertThat(delivery.getEndHubId()).isEqualTo(endHubId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.HUB_WAITING);
        assertThat(result.routeCount()).isEqualTo(1);
    }

    @Test
    void 배송_생성시_배송_경로도_함께_생성되고_담당자가_배정된다() {
        UUID orderId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, startHubId, endHubId, "서울시 송파구", "홍길동", "U01");

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(hubPort.validateHubIds(List.of(startHubId))).thenReturn(Set.of(startHubId));
        when(hubPort.validateHubIds(List.of(endHubId))).thenReturn(Set.of(endHubId));
        when(hubRoutePort.findRoute(startHubId, endHubId)).thenReturn(singleSegment(startHubId, endHubId));
        when(deliveryRepository.save(any(Delivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryManager manager = DeliveryManager.create(7L, null, "M07", ManagerType.HUB_DELIVERY_MANAGER, 2);
        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(manager);

        ArgumentCaptor<DeliveryRoute> routeCaptor = ArgumentCaptor.forClass(DeliveryRoute.class);
        when(deliveryRouteRepository.save(routeCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        deliveryCommandService.create(command);

        DeliveryRoute savedRoute = routeCaptor.getValue();
        assertThat(savedRoute.getSequence()).isEqualTo(0);
        assertThat(savedRoute.getDeliveryManagerId()).isEqualTo(7L);
        assertThat(savedRoute.getStatus()).isEqualTo(DeliveryRouteStatus.HUB_MOVE_WAITING);
    }

    @Test
    void 유효하지_않은_허브면_예외가_발생한다() {
        UUID orderId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, startHubId, endHubId, "주소", "홍길동", "U01");

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(hubPort.validateHubIds(List.of(startHubId))).thenReturn(Set.of());

        assertThatThrownBy(() -> deliveryCommandService.create(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);

        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void 이미_존재하는_주문이면_기존_배송을_그대로_반환한다() {
        UUID orderId = UUID.randomUUID();
        Delivery existing = Delivery.create(
                orderId, UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));
        when(deliveryRouteRepository.countByDeliveryId(existing.getDeliveryId())).thenReturn(1);

        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01");

        DeliveryCreateResult result = deliveryCommandService.create(command);

        assertThat(result.delivery()).isEqualTo(existing);
        assertThat(result.routeCount()).isEqualTo(1);
        verify(deliveryRepository, never()).save(any());
        verify(hubPort, never()).validateHubIds(any());
    }

    @Test
    void 배정_가능한_담당자가_없으면_예외가_전파되고_배송_경로는_저장되지_않는다() {
        UUID orderId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, startHubId, endHubId, "주소", "홍길동", "U01");

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(hubPort.validateHubIds(List.of(startHubId))).thenReturn(Set.of(startHubId));
        when(hubPort.validateHubIds(List.of(endHubId))).thenReturn(Set.of(endHubId));
        when(hubRoutePort.findRoute(startHubId, endHubId)).thenReturn(singleSegment(startHubId, endHubId));
        when(deliveryRepository.save(any(Delivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenThrow(new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_UNAVAILABLE));

        assertThatThrownBy(() -> deliveryCommandService.create(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_UNAVAILABLE);

        verify(deliveryRouteRepository, never()).save(any());
    }

    @Test
    void 여러_구간이_반환되면_구간_수만큼_경로가_생성된다() {
        UUID orderId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID midHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, startHubId, endHubId, "주소", "홍길동", "U01");

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(hubPort.validateHubIds(List.of(startHubId))).thenReturn(Set.of(startHubId));
        when(hubPort.validateHubIds(List.of(endHubId))).thenReturn(Set.of(endHubId));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<HubRoutePort.HubRouteSegment> segments = List.of(
                new HubRoutePort.HubRouteSegment(startHubId, midHubId, BigDecimal.TEN, 30),
                new HubRoutePort.HubRouteSegment(midHubId, endHubId, BigDecimal.valueOf(5), 20));
        when(hubRoutePort.findRoute(startHubId, endHubId)).thenReturn(segments);

        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null))
                .thenReturn(DeliveryManager.create(1L, null, "M01", ManagerType.HUB_DELIVERY_MANAGER, 0));
        when(deliveryRouteRepository.save(any(DeliveryRoute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryCreateResult result = deliveryCommandService.create(command);

        assertThat(result.routeCount()).isEqualTo(2);
        verify(deliveryRouteRepository, times(2)).save(any(DeliveryRoute.class));
    }

    @Test
    void hub_route_서비스_호출_실패시_예외가_전파된다() {
        UUID orderId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, startHubId, endHubId, "주소", "홍길동", "U01");

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(hubPort.validateHubIds(List.of(startHubId))).thenReturn(Set.of(startHubId));
        when(hubPort.validateHubIds(List.of(endHubId))).thenReturn(Set.of(endHubId));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FeignException feignException = mock(FeignException.class);
        lenient().when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0]);
        when(hubRoutePort.findRoute(startHubId, endHubId)).thenThrow(feignException);

        assertThatThrownBy(() -> deliveryCommandService.create(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_EXTERNAL_SERVICE_UNAVAILABLE);
    }

    @Test
    void hub_route_경로가_없으면_DELIVERY_HUB_ROUTE_NOT_FOUND_예외가_발생한다() {
        UUID orderId = UUID.randomUUID();
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        CreateDeliveryCommand command = new CreateDeliveryCommand(
                orderId, startHubId, endHubId, "주소", "홍길동", "U01");

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(hubPort.validateHubIds(List.of(startHubId))).thenReturn(Set.of(startHubId));
        when(hubPort.validateHubIds(List.of(endHubId))).thenReturn(Set.of(endHubId));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        lenient().when(notFound.getStackTrace()).thenReturn(new StackTraceElement[0]);
        when(hubRoutePort.findRoute(startHubId, endHubId)).thenThrow(notFound);

        assertThatThrownBy(() -> deliveryCommandService.create(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_HUB_ROUTE_NOT_FOUND);
    }

    // ===== changeStatus =====

    @Test
    void COMPANY_MOVING_상태에서_DELIVERED로_변경된다() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        delivery.changeStatus(DeliveryStatus.COMPANY_MOVING);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));

        DeliveryResults.DeliveryDetailResult result = deliveryCommandService.changeStatus(
                deliveryId, new ChangeDeliveryStatusCommand(DeliveryStatus.DELIVERED));

        assertThat(result.delivery().getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void COMPANY_MOVING이_아닌_상태에서_DELIVERED_요청하면_예외() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));

        assertThatThrownBy(() -> deliveryCommandService.changeStatus(
                deliveryId, new ChangeDeliveryStatusCommand(DeliveryStatus.DELIVERED)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
    }

    @Test
    void 존재하지_않는_배송이면_예외() {
        UUID deliveryId = UUID.randomUUID();
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryCommandService.changeStatus(
                deliveryId, new ChangeDeliveryStatusCommand(DeliveryStatus.DELIVERED)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }

    // ===== changeRouteStatus =====

    @Test
    void 첫_구간이_HUB_MOVING이_되면_배송_상태도_HUB_MOVING으로_동기화된다() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        DeliveryRoute route = DeliveryRoute.create(
                deliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);

        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));

        RouteStatusChangeResult result = deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.HUB_MOVING, null, null), MASTER);

        assertThat(result.route().getStatus()).isEqualTo(DeliveryRouteStatus.HUB_MOVING);
        assertThat(result.delivery().getStatus()).isEqualTo(DeliveryStatus.HUB_MOVING);
    }

    @Test
    void 잘못된_전이를_시도하면_예외() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        DeliveryRoute route = DeliveryRoute.create(
                deliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);
        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.DEST_HUB_ARRIVED, null, null), MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
    }

    @Test
    void HUB_MANAGER가_담당_허브가_아닌_구간을_변경하려하면_예외() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        DeliveryRoute route = DeliveryRoute.create(
                deliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);
        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));
        UserPrincipal otherHubManager = new UserPrincipal(5L, Role.HUB_MANAGER, UUID.randomUUID(), null);

        assertThatThrownBy(() -> deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.HUB_MOVING, null, null), otherHubManager))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }

    @Test
    void 본인이_배정되지_않은_구간을_변경하려하면_예외() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        DeliveryRoute route = DeliveryRoute.create(
                deliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);
        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));
        UserPrincipal otherManager = new UserPrincipal(99L, Role.HUB_DELIVERY_MANAGER, null, null);

        assertThatThrownBy(() -> deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.HUB_MOVING, null, null), otherManager))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }

    @Test
    void 이전_구간이_완료되지_않았으면_예외가_발생한다() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        DeliveryRoute priorRoute = DeliveryRoute.create(
                deliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);
        priorRoute.changeStatus(DeliveryRouteStatus.HUB_MOVING);
        DeliveryRoute route = DeliveryRoute.create(
                deliveryId, 1, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);

        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));
        when(deliveryRouteRepository.findAllByDeliveryId(deliveryId)).thenReturn(List.of(priorRoute, route));

        assertThatThrownBy(() -> deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.HUB_MOVING, null, null), MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);

        verify(deliveryRepository, never()).findByIdAndDeletedAtIsNull(deliveryId);
    }

    @Test
    void 마지막_구간이_도착하면_업체담당자가_배정되고_배송상태가_COMPANY_MOVING이_된다() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), endHubId, "주소", "홍길동", "U01", 1L);
        DeliveryRoute route = DeliveryRoute.create(
                deliveryId, 0, UUID.randomUUID(), endHubId, 1L, BigDecimal.TEN, 30, 1L);
        route.changeStatus(DeliveryRouteStatus.HUB_MOVING);

        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRouteRepository.countByDeliveryId(deliveryId)).thenReturn(1);

        DeliveryManager companyManager = DeliveryManager.create(9L, endHubId, "M09", ManagerType.COMPANY_DELIVERY_MANAGER, 0);
        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, endHubId))
                .thenReturn(companyManager);

        RouteStatusChangeResult result = deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.DEST_HUB_ARRIVED, null, null), MASTER);

        assertThat(result.route().getStatus()).isEqualTo(DeliveryRouteStatus.DEST_HUB_ARRIVED);
        assertThat(result.route().getActualDistance()).isEqualTo(route.getExpectedDistance());
        assertThat(result.route().getActualDuration()).isEqualTo(route.getExpectedDuration());
        assertThat(result.delivery().getStatus()).isEqualTo(DeliveryStatus.COMPANY_MOVING);
        assertThat(result.delivery().getCompanyDeliveryManagerId()).isEqualTo(9L);
    }

    @Test
    void 마지막_구간이_아니면_업체담당자_배정_없이_구간_상태만_바뀐다() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        DeliveryRoute route = DeliveryRoute.create(
                deliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);
        route.changeStatus(DeliveryRouteStatus.HUB_MOVING);

        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRouteRepository.countByDeliveryId(deliveryId)).thenReturn(2);

        RouteStatusChangeResult result = deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.DEST_HUB_ARRIVED, null, null), MASTER);

        assertThat(result.route().getStatus()).isEqualTo(DeliveryRouteStatus.DEST_HUB_ARRIVED);
        assertThat(result.delivery().getStatus()).isEqualTo(DeliveryStatus.HUB_WAITING);
        verify(deliveryManagerAssignmentService, never()).assignNextManager(any(), any());
    }

    @Test
    void actualDistance_actualDuration을_보내면_그_값이_그대로_기록된다() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), endHubId, "주소", "홍길동", "U01", 1L);
        DeliveryRoute route = DeliveryRoute.create(
                deliveryId, 0, UUID.randomUUID(), endHubId, 1L, BigDecimal.TEN, 30, 1L);
        route.changeStatus(DeliveryRouteStatus.HUB_MOVING);

        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRouteRepository.countByDeliveryId(deliveryId)).thenReturn(1);
        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, endHubId))
                .thenReturn(DeliveryManager.create(9L, endHubId, "M09", ManagerType.COMPANY_DELIVERY_MANAGER, 0));

        RouteStatusChangeResult result = deliveryCommandService.changeRouteStatus(
                deliveryId, routeId,
                new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.DEST_HUB_ARRIVED, new BigDecimal("158.40"), 115), MASTER);

        assertThat(result.route().getActualDistance()).isEqualByComparingTo("158.40");
        assertThat(result.route().getActualDuration()).isEqualTo(115);
    }

    @Test
    void 존재하지_않는_경로면_예외() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.HUB_MOVING, null, null), MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_ROUTE_NOT_FOUND);
    }

    @Test
    void 경로가_해당_배송_소속이_아니면_예외() {
        UUID deliveryId = UUID.randomUUID();
        UUID otherDeliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        DeliveryRoute route = DeliveryRoute.create(
                otherDeliveryId, 0, UUID.randomUUID(), UUID.randomUUID(), 1L, BigDecimal.TEN, 30, 1L);
        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.HUB_MOVING, null, null), MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_ROUTE_NOT_FOUND);
    }

    @Test
    void 배정_가능한_업체담당자가_없으면_예외가_전파된다() {
        UUID deliveryId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), endHubId, "주소", "홍길동", "U01", 1L);
        DeliveryRoute route = DeliveryRoute.create(
                deliveryId, 0, UUID.randomUUID(), endHubId, 1L, BigDecimal.TEN, 30, 1L);
        route.changeStatus(DeliveryRouteStatus.HUB_MOVING);

        when(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)).thenReturn(Optional.of(route));
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRouteRepository.countByDeliveryId(deliveryId)).thenReturn(1);
        when(deliveryManagerAssignmentService.assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, endHubId))
                .thenThrow(new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_UNAVAILABLE));

        assertThatThrownBy(() -> deliveryCommandService.changeRouteStatus(
                deliveryId, routeId, new ChangeDeliveryRouteStatusCommand(DeliveryRouteStatus.DEST_HUB_ARRIVED, null, null), MASTER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_UNAVAILABLE);
    }

    // ===== delete =====

    @Test
    void 정상_삭제되면_deletedAt이_채워진다() {
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = Delivery.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.of(delivery));

        deliveryCommandService.delete(deliveryId);

        assertThat(delivery.getDeletedAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_배송_삭제시_예외() {
        UUID deliveryId = UUID.randomUUID();
        when(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryCommandService.delete(deliveryId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }

    // ===== cancel =====

    @Test
    void 배송이_있으면_CANCELLED로_변경된다() {
        UUID orderId = UUID.randomUUID();
        Delivery delivery = Delivery.create(orderId, UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));

        deliveryCommandService.cancel(orderId);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
    }

    @Test
    void 배송이_이미_없어도_예외_없이_성공처리된다() {
        UUID orderId = UUID.randomUUID();
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThatCode(() -> deliveryCommandService.cancel(orderId))
                .doesNotThrowAnyException();
    }

    @Test
    void 이미_배송완료된_건은_취소할_수_없다() {
        UUID orderId = UUID.randomUUID();
        Delivery delivery = Delivery.create(orderId, UUID.randomUUID(), UUID.randomUUID(), "주소", "홍길동", "U01", 1L);
        delivery.changeStatus(DeliveryStatus.DELIVERED);
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));

        assertThatThrownBy(() -> deliveryCommandService.cancel(orderId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
    }
}
