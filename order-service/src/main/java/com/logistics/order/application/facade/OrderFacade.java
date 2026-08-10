package com.logistics.order.application.facade;

import com.logistics.order.application.dto.command.*;
import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.saga.OrderCreateOrchestrator;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.domain.entity.Order;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {
    // Facade 구현체는 Repository를 직접 의존하면 안 된다
    private final OrderCreateOrchestrator orderCreateOrchestrator;
    private final OrderCommandService orderCommandService;
    private final DeliveryClient deliveryClient;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final CompanyClient companyClient;

    // TODO: User 내부 조회 API 구현 후 실제 수령인 정보로 교체
    String receiverName = "임시 수령인";
    String receiverSlackId = "TEMP_SLACK_ID";

    public OrderCreateResult createOrder(
            OrderCreateCommand orderCreateCommand
    ){
        ProductGetResponse productGetResponse = productClient.getProduct(
                orderCreateCommand.productId()
        ).getData();

        CompanyOrderInfoResponse companyOrderInfoResponse = companyClient.getCompaniesForOrder(
                productGetResponse.companyId(),
                orderCreateCommand.endCompanyId()
        ).getData();

        OrderCreateSagaCommand orderCreateSagaCommand = new OrderCreateSagaCommand(
                orderCreateCommand,
                productGetResponse.companyId(),
                companyOrderInfoResponse.startHubId(),
                companyOrderInfoResponse.endHubId(),
                companyOrderInfoResponse.endCompanyAddress(),
                receiverName,
                receiverSlackId
        );

        return orderCreateOrchestrator.execute(orderCreateSagaCommand);
    }

    // TODO : 멱등키, orderId 사용 시 주문 생성과 주문 수정이 서로 다른 업무인데, 같은 멱등키가 되어버림
    public OrderUpdateResult updateOrder(
            OrderUpdateCommand orderUpdateCommand
    ) {
        Order order = orderCommandService.findOrderForUpdate(
                orderUpdateCommand.orderId()
        );

        Integer changeQuantity = orderUpdateCommand.quantity();

        if (changeQuantity != null) {
            CompanyOrderInfoResponse companyOrderInfoResponse = companyClient.getCompaniesForOrder(
                    order.getStartCompanyId(),
                    order.getEndCompanyId()
            ).getData();

            adjustInventory(
                    order,
                    companyOrderInfoResponse.startHubId(),
                    changeQuantity
            );
        }

        return orderCommandService.updateOrder(
                order,
                orderUpdateCommand
        );
    }

    public void deleteOrder(
            OrderDeleteCommand orderDeleteCommand
    ) {
        Order order = orderCommandService.findOrderForDelete(
                orderDeleteCommand.orderId()
        );

        CompanyOrderInfoResponse companyOrderInfoResponse = companyClient.getCompaniesForOrder(
                order.getStartCompanyId(),
                order.getEndCompanyId()
        ).getData();

        InventoryRestorationRequest inventoryRestorationRequest = new InventoryRestorationRequest(
                order.getOrderId(),
                order.getProductId(),
                companyOrderInfoResponse.startHubId(),
                order.getQuantity()
        );
        // TODO : 보상트랜잭션 순서는 차감 후 주문 실패로 한다
        inventoryClient.restoreInventory(inventoryRestorationRequest);

        orderCommandService.deleteOrder(order);
    }

    public OrderCancelResult cancelOrder(
            OrderCancelCommand orderCancelCommand
    ) {
        Order order = orderCommandService.findOrderForCancel(
                orderCancelCommand.orderId()
        );

        CompanyOrderInfoResponse companyOrderInfoResponse = companyClient.getCompaniesForOrder(
                order.getStartCompanyId(),
                order.getEndCompanyId()
        ).getData();

        deliveryClient.cancelDelivery(
                order.getOrderId()
        );

        InventoryRestorationRequest inventoryRestorationRequest = new InventoryRestorationRequest(
                order.getOrderId(),
                order.getProductId(),
                companyOrderInfoResponse.startHubId(),
                order.getQuantity()
        );

        // 실패 시
        inventoryClient.restoreInventory(
                inventoryRestorationRequest
        );

        return orderCommandService.cancelOrder(order);
    }

    private void adjustInventory(
            Order order,
            UUID hubId,
            int newQuantity
    ) {
        int quantityDifference = newQuantity - order.getQuantity();

        if (quantityDifference > 0) {
            deductInventory(order, hubId, quantityDifference);
            return;
        }

        if (quantityDifference < 0) {
            restoreInventory(order, hubId, -quantityDifference);
        }
    }

    private void deductInventory(
            Order order,
            UUID hubId,
            int quantity
    ) {
        InventoryDeductionRequest request = new InventoryDeductionRequest(
                order.getOrderId(),
                order.getProductId(),
                hubId,
                quantity
        );

        inventoryClient.deductInventory(request);
    }

    private void restoreInventory(
            Order order,
            UUID hubId,
            int quantity
    ) {
        InventoryRestorationRequest request = new InventoryRestorationRequest(
                order.getOrderId(),
                order.getProductId(),
                hubId,
                quantity
        );

        inventoryClient.restoreInventory(request);
    }
}
