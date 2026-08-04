package com.logistics.order.application.facade;

import com.logistics.order.application.dto.command.OrderCancelCommand;
import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderDeleteCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
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
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderFacade {
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
    ) {
        UUID orderId = UUID.randomUUID();

        // 상품 조회
        ProductGetResponse productGetResponse = productClient.getProduct(orderCreateCommand.productId()).getData();

        // 업체 조회
        CompanyOrderInfoResponse companyOrderInfoResponse = companyClient.getCompaniesForOrder(
                productGetResponse.companyId(),
                orderCreateCommand.endCompanyId()
        ).getData();

        // 재고
        InventoryDeductionRequest inventoryDeductionRequest = new InventoryDeductionRequest(
                orderCreateCommand.productId(),
                companyOrderInfoResponse.startHubId(),
                orderCreateCommand.quantity()
        );

        inventoryClient.deductInventory(inventoryDeductionRequest);

        // 배달
        DeliveryCreateRequest deliveryCreateRequest = new DeliveryCreateRequest(
                orderId,
                companyOrderInfoResponse.startHubId(),
                companyOrderInfoResponse.endHubId(),
                companyOrderInfoResponse.endCompanyAddress(),
                receiverName,
                receiverSlackId
        );

        DeliveryCreateResponse deliveryCreateResponse = deliveryClient.createDelivery(deliveryCreateRequest).getData();

        return orderCommandService.createOrder(
                orderCreateCommand,
                orderId,
                deliveryCreateResponse.deliveryId(),
                productGetResponse.companyId()
        );
    }

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
        orderCommandService.deleteOrder(orderDeleteCommand);
    }

    public OrderCancelResult cancelOrder(
            OrderCancelCommand orderCancelCommand
    ) {
        return orderCommandService.cancelOrder(orderCancelCommand);
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
                order.getProductId(),
                hubId,
                quantity
        );

        inventoryClient.restoreInventory(request);
    }

}
