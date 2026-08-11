package com.logistics.order.application.facade;

import com.logistics.order.application.authorization.OrderAuthorizationService;
import com.logistics.order.application.dto.auth.AuthenticatedUser;
import com.logistics.order.application.dto.command.OrderCancelCommand;
import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderDeleteCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.*;
import com.logistics.order.application.port.CompanyPort;
import com.logistics.order.application.port.ProductPort;
import com.logistics.order.application.port.UserPort;
import com.logistics.order.application.saga.OrderCancelOrchestrator;
import com.logistics.order.application.saga.OrderCreateOrchestrator;
import com.logistics.order.application.saga.OrderDeleteOrchestrator;
import com.logistics.order.application.saga.OrderUpdateOrchestrator;
import com.logistics.order.application.saga.command.OrderCancelSagaCommand;
import com.logistics.order.application.saga.command.OrderCreateSagaCommand;
import com.logistics.order.application.saga.command.OrderDeleteSagaCommand;
import com.logistics.order.application.saga.command.OrderUpdateSagaCommand;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {
    UUID orderId = UUID.randomUUID();
    UUID deliveryId = UUID.randomUUID();
    UUID startCompanyId = UUID.randomUUID();
    UUID endCompanyId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    UUID startHubId = UUID.randomUUID();
    UUID endHubId = UUID.randomUUID();
    UUID idempotencyKey = UUID.randomUUID();

    AuthenticatedUser authenticatedUser = new AuthenticatedUser(
            1L,
            Role.MASTER,
            null,
            null
    );
    @Mock
    private OrderCommandService orderCommandService;

    @Mock
    private OrderAuthorizationService orderAuthorizationService;

    @Mock
    private ProductPort productPort;
    @Mock
    private CompanyPort companyPort;
    @Mock
    private UserPort userPort;
    @Mock
    private OrderCreateOrchestrator orderCreateOrchestrator;
    @Mock
    private OrderUpdateOrchestrator orderUpdateOrchestrator;
    @Mock
    private OrderDeleteOrchestrator orderDeleteOrchestrator;
    @Mock
    private OrderCancelOrchestrator orderCancelOrchestrator;
    @InjectMocks
    private OrderFacade orderFacade;

    @Nested
    @DisplayName("주문 생성")
    class order_create {
        @Test
        @DisplayName("주문 생성 요청 위임 성공")
        void order_create_success() {
            OrderCreateCommand orderCreateCommand = new OrderCreateCommand(
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );
            ProductGetResult productGetResult = mock(ProductGetResult.class);
            CompanyOrderInfoResult companyOrderInfoResult = mock(CompanyOrderInfoResult.class);
            UserInfoResult userInfoResult = mock(UserInfoResult.class);
            OrderCreateResult orderCreateResult = mock(OrderCreateResult.class);

            given(productPort.getProduct(productId)).willReturn(productGetResult);
            given(productGetResult.companyId()).willReturn(startCompanyId);
            given(companyPort.getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            )).willReturn(companyOrderInfoResult);

            given(companyOrderInfoResult.startHubId()).willReturn(startHubId);
            given(companyOrderInfoResult.endHubId()).willReturn(endHubId);
            given(companyOrderInfoResult.endCompanyAddress()).willReturn("서울특별시 송파구");
            given(orderCreateOrchestrator.execute(any(OrderCreateSagaCommand.class))).willReturn(orderCreateResult);
            given(userPort.getUser(authenticatedUser.userId())).willReturn(userInfoResult);
            given(userInfoResult.name()).willReturn("name");
            given(userInfoResult.slackId()).willReturn("U123456");

            orderFacade.createOrder(
                    orderCreateCommand,
                    idempotencyKey,
                    authenticatedUser
            );

            verify(productPort).getProduct(productId);
            verify(companyPort).getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            );
            verify(userPort).getUser(authenticatedUser.userId());
            verify(orderCreateOrchestrator).execute(any(OrderCreateSagaCommand.class));
        }

        @Test
        @DisplayName("수량 변경이 없으면 업체 조회 없이 수정 요청 위임")
        void order_update_without_quantity() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );
            OrderUpdateCommand orderUpdateCommand = new OrderUpdateCommand(
                    orderId,
                    null,
                    "요청사항만 변경"
            );
            given(orderCommandService.findOrderForUpdate(orderId)).willReturn(order);

            orderFacade.updateOrder(
                    orderUpdateCommand,
                    authenticatedUser
            );

            verify(orderCommandService).findOrderForUpdate(orderId);
            verify(orderUpdateOrchestrator).execute(any(OrderUpdateSagaCommand.class));
        }
    }

    @Nested
    @DisplayName("주문 수정")
    class order_update {
        @Test
        @DisplayName("주문 수정 요청 위임 성공")
        void order_update_success() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );
            OrderUpdateCommand orderUpdateCommand = new OrderUpdateCommand(
                    orderId,
                    130,
                    "오전까지 납품"
            );
            CompanyOrderInfoResult companyOrderInfoResult = mock(CompanyOrderInfoResult.class);
            OrderUpdateResult orderUpdateResult = mock(OrderUpdateResult.class);

            given(orderCommandService.findOrderForUpdate(orderId)).willReturn(order);
            given(companyPort.getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            )).willReturn(companyOrderInfoResult);

            given(companyOrderInfoResult.startHubId()).willReturn(startHubId);
            given(orderUpdateOrchestrator.execute(any(OrderUpdateSagaCommand.class))).willReturn(orderUpdateResult);

            orderFacade.updateOrder(
                    orderUpdateCommand,
                    authenticatedUser
            );

            verify(orderCommandService).findOrderForUpdate(orderId);
            verify(companyPort).getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            );
            verify(orderUpdateOrchestrator).execute(any(OrderUpdateSagaCommand.class));
        }
    }

    @Nested
    @DisplayName("주문 삭제")
    class order_delete {
        @Test
        @DisplayName("주문 삭제 요청 위임 성공")
        void order_delete_success() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );
            OrderDeleteCommand orderDeleteCommand = new OrderDeleteCommand(orderId);
            CompanyOrderInfoResult companyOrderInfoResult = mock(CompanyOrderInfoResult.class);

            given(orderCommandService.findOrderForDelete(orderId)).willReturn(order);
            given(companyPort.getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            )).willReturn(companyOrderInfoResult);

            given(companyOrderInfoResult.startHubId()).willReturn(startHubId);

            orderFacade.deleteOrder(
                    orderDeleteCommand,
                    authenticatedUser
            );

            verify(orderCommandService).findOrderForDelete(orderId);
            verify(companyPort).getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            );
            verify(orderDeleteOrchestrator).execute(any(OrderDeleteSagaCommand.class));
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class order_cancel {
        @Test
        @DisplayName("주문 취소 요청 위임 성공")
        void order_cancel_success() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );
            OrderCancelCommand orderCancelCommand = new OrderCancelCommand(orderId);
            CompanyOrderInfoResult companyOrderInfoResult = mock(CompanyOrderInfoResult.class);
            OrderCancelResult orderCancelResult = mock(OrderCancelResult.class);

            given(orderCommandService.findOrderForCancel(orderId)).willReturn(order);
            given(companyPort.getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            )).willReturn(companyOrderInfoResult);

            given(companyOrderInfoResult.startHubId()).willReturn(startHubId);
            given(orderCancelOrchestrator.execute(any(OrderCancelSagaCommand.class))).willReturn(orderCancelResult);

            orderFacade.cancelOrder(
                    orderCancelCommand,
                    authenticatedUser
            );

            verify(orderCommandService).findOrderForCancel(orderId);
            verify(companyPort).getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            );
            verify(orderCancelOrchestrator).execute(any(OrderCancelSagaCommand.class));
        }
    }
}