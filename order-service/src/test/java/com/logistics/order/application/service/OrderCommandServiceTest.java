package com.logistics.order.application.service;

import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.port.EventPublisher;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.entity.OrderStatus;
import com.logistics.order.domain.repository.OrderCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    private OrderCommandRepository orderCommandRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OrderCommandService orderCommandService;

    @Nested
    @DisplayName("주문 생성")
    class order_create {
        @Test
        @DisplayName("주문 생성하고 저장 성공")
        void order_create_success() {
            UUID orderId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID endCompanyId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            OrderCreateCommand orderCreateCommand = new OrderCreateCommand(
                    endCompanyId,
                    productId,
                    10,
                    "8월 6일 오전까지 납품"
            );

            given(orderCommandRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            OrderCreateResult result = orderCommandService.createOrder(
                    orderCreateCommand,
                    orderId,
                    deliveryId,
                    deliveryId
            );

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

            verify(orderCommandRepository).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getOrderId()).isEqualTo(orderId);
            assertThat(savedOrder.getDeliveryId()).isEqualTo(deliveryId);
            assertThat(savedOrder.getEndCompanyId()).isEqualTo(endCompanyId);
            assertThat(savedOrder.getProductId()).isEqualTo(productId);
            assertThat(savedOrder.getQuantity()).isEqualTo(10);
            assertThat(savedOrder.getRequest()).isEqualTo("8월 6일 오전까지 납품");
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);

            assertThat(result.orderId()).isEqualTo(orderId);
        }
    }
}