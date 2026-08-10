package com.logistics.order.application.facade;

import com.logistics.order.application.dto.command.*;
import com.logistics.order.application.dto.result.*;
import com.logistics.order.application.port.CompanyPort;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.port.ProductPort;
import com.logistics.order.application.saga.OrderCreateOrchestrator;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.infrastructure.feign.request.InventoryRestorationRequest;
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
    private final DeliveryPort deliveryPort;
    private final ProductPort productPort;
    private final InventoryPort inventoryPort;
    private final CompanyPort companyPort;

    // TODO: User 내부 조회 API 구현 후 실제 수령인 정보로 교체
    String receiverName = "임시 수령인";
    String receiverSlackId = "TEMP_SLACK_ID";

    public OrderCreateResult createOrder(
            OrderCreateCommand orderCreateCommand
    ) {
        ProductGetResult productGetResult = productPort.getProduct(
                orderCreateCommand.productId()
        );

        CompanyOrderInfoResult companyOrderInfoResult = companyPort.getCompaniesForOrder(
                productGetResult.companyId(),
                orderCreateCommand.endCompanyId()
        );

        OrderCreateSagaCommand orderCreateSagaCommand = new OrderCreateSagaCommand(
                orderCreateCommand,
                productGetResult.companyId(),
                companyOrderInfoResult.startHubId(),
                companyOrderInfoResult.endHubId(),
                companyOrderInfoResult.endCompanyAddress(),
                receiverName,
                receiverSlackId
        );

        return orderCreateOrchestrator.execute(orderCreateSagaCommand);
    }

    // TODO : 멱등키, orderId 사용 시 주문 생성과 주문 수정이 서로 다른 업무인데, 같은 멱등키가 되어버림
    // orderId = 주문 자체의 정체성
    // 멱등키 = 이번 요청 하나의 정체성(이 작업 요청이 누구냐) 따라서 변경 필요
    public OrderUpdateResult updateOrder(
            OrderUpdateCommand orderUpdateCommand
    ) {
        Order order = orderCommandService.findOrderForUpdate(
                orderUpdateCommand.orderId()
        );
        UUID operationId = UUID.randomUUID();
        Integer changeQuantity = orderUpdateCommand.quantity();

        if (changeQuantity != null) {
            CompanyOrderInfoResult companyOrderInfoResult = companyPort.getCompaniesForOrder(
                    order.getStartCompanyId(),
                    order.getEndCompanyId()
            );

            adjustInventory(
                    operationId,
                    order,
                    companyOrderInfoResult.startHubId(),
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
        UUID operationId = UUID.randomUUID();

        Order order = orderCommandService.findOrderForDelete(
                orderDeleteCommand.orderId()
        );

        CompanyOrderInfoResult companyOrderInfoResult = companyPort.getCompaniesForOrder(
                order.getStartCompanyId(),
                order.getEndCompanyId()
        );
        inventoryPort.restoreInventory(
                operationId,
                order.getOrderId(),
                order.getProductId(),
                companyOrderInfoResult.startHubId(),
                order.getQuantity()
        );

        orderCommandService.deleteOrder(order);
    }

    public OrderCancelResult cancelOrder(
            OrderCancelCommand orderCancelCommand
    ) {
        UUID operationId = UUID.randomUUID();

        Order order = orderCommandService.findOrderForCancel(
                orderCancelCommand.orderId()
        );

        CompanyOrderInfoResult companyOrderInfoResult = companyPort.getCompaniesForOrder(
                order.getStartCompanyId(),
                order.getEndCompanyId()
        );

        deliveryPort.cancelDelivery(
                order.getOrderId()
        );
        // 실패 시
        inventoryPort.restoreInventory(
                operationId,
                order.getOrderId(),
                order.getProductId(),
                companyOrderInfoResult.startHubId(),
                order.getQuantity()
        );

        return orderCommandService.cancelOrder(order);
    }

    private void adjustInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int newQuantity
    ) {
        int quantityDifference = newQuantity - order.getQuantity();

        if (quantityDifference > 0) {
            deductInventory(
                    operationId,
                    order,
                    hubId,
                    quantityDifference
            );
            return;
        }

        if (quantityDifference < 0) {
            restoreInventory(
                    operationId,
                    order,
                    hubId,
                    -quantityDifference
            );
        }
    }

    private void deductInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int quantity
    ) {
        inventoryPort.deductInventory(
                operationId,
                order.getOrderId(),
                order.getProductId(),
                hubId,
                quantity
        );
    }

    private void restoreInventory(
            UUID operationId,
            Order order,
            UUID hubId,
            int quantity
    ) {
        inventoryPort.restoreInventory(
                operationId,
                order.getOrderId(),
                order.getProductId(),
                hubId,
                quantity
        );
    }
}
