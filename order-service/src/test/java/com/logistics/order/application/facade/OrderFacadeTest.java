package com.logistics.order.application.facade;

import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderCreateSagaCommand;
import com.logistics.order.application.dto.command.OrderDeleteCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.saga.OrderCreateOrchestrator;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.feign.client.CompanyClient;
import com.logistics.order.infrastructure.feign.client.DeliveryClient;
import com.logistics.order.infrastructure.feign.client.InventoryClient;
import com.logistics.order.infrastructure.feign.client.ProductClient;
import com.logistics.order.infrastructure.feign.request.DeliveryCreateRequest;
import com.logistics.order.infrastructure.feign.request.InventoryDeductionRequest;
import com.logistics.order.infrastructure.feign.request.InventoryRestorationRequest;
import com.logistics.order.infrastructure.feign.response.CompanyOrderInfoResponse;
import com.logistics.order.infrastructure.feign.response.DeliveryCreateResponse;
import com.logistics.order.infrastructure.feign.response.ProductGetResponse;
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
    // TODO : CRUD 이후 예외 관련 테스트 진행 예정
    UUID orderId = UUID.randomUUID();
    UUID deliveryId = UUID.randomUUID();
    UUID startCompanyId = UUID.randomUUID();
    UUID endCompanyId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    UUID startHubId = UUID.randomUUID();
    UUID endHubId = UUID.randomUUID();

    @Mock
    private OrderCommandService orderCommandService;

    @Mock
    private ProductClient productClient;

    @Mock
    private CompanyClient companyClient;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderFacade orderFacade;

    @Mock
    private OrderCreateOrchestrator orderCreateOrchestrator;

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

            ProductGetResponse productGetResponse = mock(ProductGetResponse.class);
            CompanyOrderInfoResponse companyOrderInfoResponse = mock(CompanyOrderInfoResponse.class);
            OrderCreateResult orderCreateResult = mock(OrderCreateResult.class);
            ApiResponse<ProductGetResponse> productApiResponse = mock(ApiResponse.class);
            ApiResponse<CompanyOrderInfoResponse> companyApiResponse = mock(ApiResponse.class);

            given(productClient.getProduct(productId)).willReturn(productApiResponse);
            given(productApiResponse.getData()).willReturn(productGetResponse);
            given(productGetResponse.companyId()).willReturn(startCompanyId);

            given(companyClient.getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            )).willReturn(companyApiResponse);
            given(companyApiResponse.getData()).willReturn(companyOrderInfoResponse);
            given(companyOrderInfoResponse.startHubId()).willReturn(startHubId);
            given(companyOrderInfoResponse.endHubId()).willReturn(endHubId);
            given(companyOrderInfoResponse.endCompanyAddress()).willReturn("서울특별시 송파구");

            given(orderCreateOrchestrator.execute(any(OrderCreateSagaCommand.class))).willReturn(orderCreateResult);

            orderFacade.createOrder(orderCreateCommand);

            verify(productClient).getProduct(productId);
            verify(companyClient).getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            );
            verify(orderCreateOrchestrator).execute(any(OrderCreateSagaCommand.class));
        }

        @Test
        @DisplayName("업체 조회 실패 시 이후 호출 중단")
        void order_create_company() {

        }

        @Test
        @DisplayName("재고 차감 실패 시 배송과 주문 생성 중단")
        void order_create_inventory() {

        }

        @Test
        @DisplayName("배송 생성 실패 시 주문 저장 중단")
        void order_create_fail_save() {

        }
    }

    @Nested
    @DisplayName("주문 수정")
    class order_update {
        @Test
        @DisplayName("주문 수정 성공")
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

            OrderUpdateCommand command = new OrderUpdateCommand(
                    orderId,
                    130,
                    "오전까지 납품"
            );

            CompanyOrderInfoResponse companyResponse = mock(CompanyOrderInfoResponse.class);
            given(orderCommandService.findOrderForUpdate(orderId)).willReturn(order);
            ApiResponse<CompanyOrderInfoResponse> apiResponse = mock(ApiResponse.class);

            given(companyClient.getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            )).willReturn(apiResponse);

            given(apiResponse.getData()).willReturn(companyResponse);
            given(companyResponse.startHubId()).willReturn(startHubId);

            orderFacade.updateOrder(command);

            verify(inventoryClient).deductInventory(any(InventoryDeductionRequest.class));
            verify(orderCommandService).updateOrder(
                    order,
                    command
            );
        }
    }

    @Nested
    @DisplayName("주문 삭제")
    class order_delete {
        @Test
        @DisplayName("주문 삭제 성공")
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
            CompanyOrderInfoResponse companyOrderInfoResponse = mock(CompanyOrderInfoResponse.class);

            given(orderCommandService.findOrderForDelete(orderId)).willReturn(order);

            ApiResponse<CompanyOrderInfoResponse> apiResponse = mock(ApiResponse.class);
            given(companyClient.getCompaniesForOrder(
                    startCompanyId,
                    endCompanyId
            )).willReturn(apiResponse);
            given(apiResponse.getData()).willReturn(companyOrderInfoResponse);

            given(companyOrderInfoResponse.startHubId()).willReturn(startHubId);

            orderFacade.deleteOrder(orderDeleteCommand);

            verify(inventoryClient).restoreInventory(any(InventoryRestorationRequest.class));
            verify(orderCommandService).deleteOrder(order);
        }
    }
}