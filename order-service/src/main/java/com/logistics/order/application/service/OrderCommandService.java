package com.logistics.order.application.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.entity.OutboxEvent;
import com.logistics.order.domain.repository.OrderCommandRepository;
import com.logistics.order.domain.repository.OutboxRepository;
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
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OrderCreateResult createOrder(
            OrderCreateCommand orderCreateCommand,
            UUID orderId,
            UUID deliveryId,
            UUID startCompanyId,
            String receiverName,
            String receiverSlackId
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

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderId(),
                savedOrder.getDeliveryId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getRequest(),
                receiverName,
                receiverSlackId,
                savedOrder.getCreatedAt()
        );

        OutboxEvent outboxEvent = OutboxEvent.create(
                savedOrder.getOrderId(),
                "ORDER_CREATED",
                serialize(event)
        );

        outboxRepository.save(outboxEvent);

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
            Order order,
            Long deletedBy
    ) {
        order.delete(deletedBy);
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

    private String serialize(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "주문 생성 이벤트 직렬화에 실패했습니다.",
                    e
            );
        }
    }
}
