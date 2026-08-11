package com.logistics.order.application.service;

import com.logistics.order.application.authorization.OrderAuthorizationService;
import com.logistics.order.application.dto.auth.AuthenticatedUser;
import com.logistics.order.application.dto.query.OrderSearchQuery;
import com.logistics.order.application.dto.result.DeliveryGetResult;
import com.logistics.order.application.dto.result.OrderListResult;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.entity.Role;
import com.logistics.order.domain.repository.OrderQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {
    @Mock
    private OrderQueryRepository orderQueryRepository;

    @Mock
    private OrderAuthorizationService orderAuthorizationService;

    @Mock
    private DeliveryPort deliveryPort;

    @InjectMocks
    private OrderQueryService orderQueryService;

    @Nested
    @DisplayName("전체조회")
    class getAll {
        @Test
        @DisplayName("배송담당자는 본인이 담당하는 주문만 조회")
        void getAll_delivery_only_assignedOrders() {
            Long userId = 1L;
            UUID assignedDeliveryId = UUID.randomUUID();
            UUID otherDeliveryId = UUID.randomUUID();
            Order assignedOrder = mock(Order.class);
            Order otherOrder = mock(Order.class);

            given(assignedOrder.getDeliveryId()).willReturn(assignedDeliveryId);
            given(otherOrder.getDeliveryId()).willReturn(otherDeliveryId);

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.HUB_DELIVERY_MANAGER,
                    null,
                    null
            );

            OrderSearchQuery query = new OrderSearchQuery(
                    null,
                    null,
                    "createdAt",
                    0,
                    10
            );

            Pageable pageable = PageRequest.of(
                    0,
                    10,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
            Page<Order> searchedOrders = new PageImpl<>(
                    List.of(assignedOrder, otherOrder),
                    pageable,
                    2
            );

            given(orderQueryRepository.search(null, null, pageable)).willReturn(searchedOrders);

            DeliveryGetResult assignedDelivery = mock(DeliveryGetResult.class);
            DeliveryGetResult otherDelivery = mock(DeliveryGetResult.class);

            given(assignedDelivery.deliveryManagerId()).willReturn(userId);
            given(otherDelivery.deliveryManagerId()).willReturn(999L);

            given(deliveryPort.getDelivery(assignedDeliveryId)).willReturn(assignedDelivery);
            given(deliveryPort.getDelivery(otherDeliveryId)).willReturn(otherDelivery);

            OrderListResult result = orderQueryService.getOrders(query, authenticatedUser);

            assertThat(result).isNotNull();

            verify(orderQueryRepository).search(
                    null,
                    null,
                    pageable
            );

            verify(deliveryPort).getDelivery(assignedDeliveryId);
            verify(deliveryPort).getDelivery(otherDeliveryId);
        }

        @Test
        @DisplayName("배송담당자의 담당 주문만 결과에 포함")
        void getAll_delivery_filters_only_assignedOrders() {
            Long userId = 1L;
            UUID assignedOrderId = UUID.randomUUID();
            UUID assignedDeliveryId = UUID.randomUUID();
            UUID otherDeliveryId = UUID.randomUUID();
            Order assignedOrder = mock(Order.class);
            Order otherOrder = mock(Order.class);

            given(assignedOrder.getOrderId()).willReturn(assignedOrderId);
            given(assignedOrder.getDeliveryId()).willReturn(assignedDeliveryId);
            given(otherOrder.getDeliveryId()).willReturn(otherDeliveryId);

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    Role.HUB_DELIVERY_MANAGER,
                    null,
                    null
            );

            OrderSearchQuery query = new OrderSearchQuery(
                    null,
                    null,
                    "createdAt",
                    0,
                    10
            );

            Pageable pageable = PageRequest.of(
                    0,
                    10,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

            Page<Order> searchedOrders = new PageImpl<>(
                    List.of(assignedOrder, otherOrder),
                    pageable,
                    2
            );

            given(orderQueryRepository.search(null, null, pageable)).willReturn(searchedOrders);

            DeliveryGetResult assignedDelivery = mock(DeliveryGetResult.class);
            DeliveryGetResult otherDelivery = mock(DeliveryGetResult.class);

            given(assignedDelivery.deliveryManagerId()).willReturn(userId);
            given(otherDelivery.deliveryManagerId()).willReturn(999L);

            given(deliveryPort.getDelivery(assignedDeliveryId)).willReturn(assignedDelivery);

            given(deliveryPort.getDelivery(otherDeliveryId)).willReturn(otherDelivery);

            OrderListResult result = orderQueryService.getOrders(query, authenticatedUser);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().getFirst().orderId()).isEqualTo(assignedOrderId);
        }
    }
}