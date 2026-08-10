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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderCreateOrchestrator orderCreateOrchestrator;
    private final OrderUpdateOrchestrator orderUpdateOrchestrator;
    private final OrderDeleteOrchestrator orderDeleteOrchestrator;
    private final OrderCancelOrchestrator orderCancelOrchestrator;

    private final OrderAuthorizationService orderAuthorizationService;
    private final OrderCommandService orderCommandService;

    private final ProductPort productPort;
    private final CompanyPort companyPort;

    // TODO: User 내부 조회 API 구현 후 실제 수령인 정보로 교체
    String receiverName = "임시 수령인";
    String receiverSlackId = "TEMP_SLACK_ID";

    public OrderCreateResult createOrder(
            OrderCreateCommand orderCreateCommand,
            UUID idempotencyKey
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
                receiverSlackId,
                idempotencyKey
        );

        return orderCreateOrchestrator.execute(orderCreateSagaCommand);
    }

    public OrderUpdateResult updateOrder(
            OrderUpdateCommand orderUpdateCommand,
            AuthenticatedUser authenticatedUser
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

        orderAuthorizationService.validateHubAccess(
                authenticatedUser,
                companyOrderInfoResult.startHubId()
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
            OrderDeleteCommand orderDeleteCommand,
            AuthenticatedUser authenticatedUser
    ) {
        Order order = orderCommandService.findOrderForDelete(
                orderDeleteCommand.orderId()
        );

        CompanyOrderInfoResult companyOrderInfoResult = companyPort.getCompaniesForOrder(
                order.getStartCompanyId(),
                order.getEndCompanyId()
        );

        orderAuthorizationService.validateHubAccess(
                authenticatedUser,
                companyOrderInfoResult.startHubId()
        );

        OrderDeleteSagaCommand orderDeleteSagaCommand = new OrderDeleteSagaCommand(
                order,
                companyOrderInfoResult.startHubId(),
                authenticatedUser.userId()
        );

        orderDeleteOrchestrator.execute(
                orderDeleteSagaCommand
        );
    }

    public OrderCancelResult cancelOrder(
            OrderCancelCommand orderCancelCommand,
            AuthenticatedUser authenticatedUser
    ) {
        Order order = orderCommandService.findOrderForCancel(
                orderCancelCommand.orderId()
        );

        CompanyOrderInfoResult companyOrderInfoResult = companyPort.getCompaniesForOrder(
                order.getStartCompanyId(),
                order.getEndCompanyId()
        );

        orderAuthorizationService.validateHubAccess(
                authenticatedUser,
                companyOrderInfoResult.startHubId()
        );

        OrderCancelSagaCommand orderCancelSagaCommand = new OrderCancelSagaCommand(
                order,
                companyOrderInfoResult.startHubId()
        );

        return orderCancelOrchestrator.execute(orderCancelSagaCommand);
    }
}
