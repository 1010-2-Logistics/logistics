package com.logistics.order.application.facade;

import com.logistics.order.application.dto.command.*;
import com.logistics.order.application.dto.result.*;
import com.logistics.order.application.port.CompanyPort;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.port.ProductPort;
import com.logistics.order.application.saga.OrderCreateOrchestrator;
import com.logistics.order.application.saga.OrderUpdateOrchestrator;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.domain.entity.Order;
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
    private final OrderUpdateOrchestrator orderUpdateOrchestrator;

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

    public OrderUpdateResult updateOrder(
            OrderUpdateCommand orderUpdateCommand
    ) {
        Order order = orderCommandService.findOrderForUpdate(
                orderUpdateCommand.orderId()
        );

        if (orderUpdateCommand.quantity() == null) {
            return orderUpdateOrchestrator.execute(
                    new OrderUpdateSagaCommand(
                            order,
                            orderUpdateCommand,
                            null
                    )
            );
        }

        CompanyOrderInfoResult companyOrderInfoResult = companyPort.getCompaniesForOrder(
                order.getStartCompanyId(),
                order.getEndCompanyId()
        );

        OrderUpdateSagaCommand orderUpdateSagaCommand = new OrderUpdateSagaCommand(
                order,
                orderUpdateCommand,
                companyOrderInfoResult.startHubId()
        );

        return orderUpdateOrchestrator.execute(
                orderUpdateSagaCommand
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
}
