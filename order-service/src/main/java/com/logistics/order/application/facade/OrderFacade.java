package com.logistics.order.application.facade;

import com.logistics.order.application.dto.command.OrderCancelCommand;
import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderDeleteCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.service.OrderCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Controller가 바라보는 단일 진입점. 여러 서비스를 조합해야 하는 복잡한 유스케이스에서만 쓰고,
// 단순 CRUD는 Controller가 SampleCommandService/SampleQueryService를 바로 호출해도 됩니다.
@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderCommandService orderCommandService;
    //private final ProductClient productClient;
    //private final CompanyClient companyClient;
    //private final InventoryClient inventoryClient;
    public OrderCreateResult createOrder(
            OrderCreateCommand orderCreateCommand
    ) {
        return orderCommandService.createOrder(orderCreateCommand);
    }

    public OrderUpdateResult updateOrder(
            OrderUpdateCommand orderUpdateCommand
    ) {
        return orderCommandService.updateOrder(orderUpdateCommand);
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
}
