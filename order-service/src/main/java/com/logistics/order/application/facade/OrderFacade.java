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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {
    // Facade 구현체는 Repository를 직접 의존하면 안 된다

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
                orderId,
                orderCreateCommand.productId(),
                companyOrderInfoResponse.startHubId(),
                orderCreateCommand.quantity()
        );

        inventoryClient.deductInventory(inventoryDeductionRequest);
        DeliveryCreateResponse deliveryCreateResponse;
        // 배송
        DeliveryCreateRequest deliveryCreateRequest = new DeliveryCreateRequest(
                orderId,
                companyOrderInfoResponse.startHubId(),
                companyOrderInfoResponse.endHubId(),
                companyOrderInfoResponse.endCompanyAddress(),
                receiverName,
                receiverSlackId
        );

        try {
            deliveryCreateResponse = deliveryClient.createDelivery(deliveryCreateRequest).getData();

        } catch (RuntimeException originalException) {
            // 배송 생성 실패 → 앞에서 차감한 재고 복원
            try {
                InventoryRestorationRequest restorationRequest =
                        new InventoryRestorationRequest(
                                orderId,
                                orderCreateCommand.productId(),
                                companyOrderInfoResponse.startHubId(),
                                orderCreateCommand.quantity()
                        );

                inventoryClient.restoreInventory(restorationRequest);

            } catch (RuntimeException compensationException) {
                log.error(
                        "배송 생성 실패 보상 중 재고 복원 실패. orderId={}",
                        orderId,
                        compensationException
                );

                originalException.addSuppressed(compensationException);
            }

            throw originalException;
        }
        // 주문 저장
        try {
            return orderCommandService.createOrder(
                    orderCreateCommand,
                    orderId,
                    deliveryCreateResponse.deliveryId(),
                    productGetResponse.companyId()
            );

        } catch (RuntimeException originalException) {
            // 주문 저장 실패 → 이미 생성된 배송 취소
            try {
                deliveryClient.cancelDelivery(orderId);

            } catch (RuntimeException compensationException) {
                log.error(
                        "주문 저장 실패 보상 중 배송 취소 실패. orderId={}",
                        orderId,
                        compensationException
                );

                originalException.addSuppressed(compensationException);
            }

            // 주문 저장 실패 → 이미 차감된 재고 복원
            try {
                InventoryRestorationRequest restorationRequest =
                        new InventoryRestorationRequest(
                                orderId,
                                orderCreateCommand.productId(),
                                companyOrderInfoResponse.startHubId(),
                                orderCreateCommand.quantity()
                        );

                inventoryClient.restoreInventory(restorationRequest);

            } catch (RuntimeException compensationException) {
                log.error(
                        "주문 저장 실패 보상 중 재고 복원 실패. orderId={}",
                        orderId,
                        compensationException
                );

                originalException.addSuppressed(compensationException);
            }

            throw originalException;
        }
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
