package com.logistics.order.application.service;


import com.logistics.order.application.dto.command.OrderCancelCommand;
import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderDeleteCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.port.EventPublisher;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.repository.OrderCommandRepository;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private final OrderCommandRepository orderCommandRepository;
    private final EventPublisher eventPublisher;

    public OrderCreateResult createOrder(
            OrderCreateCommand orderCreateCommand,
            UUID orderId,
            UUID deliveryId,
            UUID startCompanyId
    ) {
        Order order = Order.create(
                orderId,
                deliveryId,
                startCompanyId,
                orderCreateCommand.endCompanyId(),
                orderCreateCommand.productId(),
                orderCreateCommand.quantity(),
                orderCreateCommand.request()
        );

        Order savedOrder = orderCommandRepository.save(order);

        return OrderCreateResult.from(savedOrder);
    }

    public Order findOrderForUpdate(
            UUID orderId
    ) {
        Order order = orderCommandRepository
                .findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() ->
                        new CustomException(OrderErrorCode.ORDER_NOT_FOUND)
                );

        // TODO: errorCode update -> ORDER_ALREADY_CANCELED
        if (order.isCanceled()) {
            throw new CustomException(OrderErrorCode.ORDER_NOT_FOUND);
        }

        return order;
    }

    public OrderUpdateResult updateOrder(
            Order order,
            OrderUpdateCommand orderUpdateCommand
    ) {
        order.update(
                orderUpdateCommand.quantity(),
                orderUpdateCommand.request()
        );

        Order savedOrder = orderCommandRepository.save(order);

        return OrderUpdateResult.from(savedOrder);
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
