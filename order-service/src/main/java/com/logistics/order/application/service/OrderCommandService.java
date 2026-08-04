package com.logistics.order.application.service;


import com.logistics.order.application.dto.command.OrderCancelCommand;
import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderDeleteCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.port.EventPublisher;
import com.logistics.order.domain.repository.OrderCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private final OrderCommandRepository orderCommandRepository;
    private final EventPublisher eventPublisher;

    public OrderCreateResult createOrder(
            OrderCreateCommand orderCreateCommand
    ) {
        return null;
    }
    public OrderUpdateResult updateOrder(
            OrderUpdateCommand orderUpdateCommand
    ) {
        return null;
    }

    public void deleteOrder(
            OrderDeleteCommand orderDeleteCommand
    ) {

    }

    public OrderCancelResult cancelOrder(
            OrderCancelCommand orderCancelCommand
    ) {
        return null;
    }
}
