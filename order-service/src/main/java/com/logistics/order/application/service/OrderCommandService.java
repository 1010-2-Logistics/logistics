package com.logistics.order.application.service;


import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.repository.OrderCommandRepository;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {
    private final OrderCommandRepository orderCommandRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

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

        applicationEventPublisher.publishEvent(
                new OrderCreatedEvent(
                        savedOrder.getOrderId(),
                        savedOrder.getDeliveryId(),
                        savedOrder.getProductId(),
                        savedOrder.getQuantity(),
                        savedOrder.getCreatedAt()
                )
        );

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

        if (order.isCanceled()) {
            throw new CustomException(OrderErrorCode.ORDER_ALREADY_CANCELED);
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
            Order order
    ) {
        // TODO : 인증 구현 후 실사용자 ID로 변경
        order.delete(null);
        orderCommandRepository.save(order);
    }

    public Order findOrderForDelete(
            UUID orderId
    ) {
        Order order = orderCommandRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.isDeleted()) {
            throw new CustomException(OrderErrorCode.ORDER_DELETE_CONFLICT);
        }

        return order;
    }

    public OrderCancelResult cancelOrder(
            Order order
    ) {
        order.cancel();

        Order savedOrder = orderCommandRepository.save(order);

        return OrderCancelResult.from(savedOrder);
    }

    public Order findOrderForCancel(
            UUID orderId
    ) {
        Order order = orderCommandRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.isCanceled()) {
            throw new CustomException(OrderErrorCode.ORDER_CANCEL_CONFLICT);
        }

        return order;
    }
}
