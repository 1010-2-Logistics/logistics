package com.logistics.order.application.service;

import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.entity.OrderStatus;
import com.logistics.order.domain.repository.OrderCommandRepository;
import com.logistics.order.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {
    UUID orderId = UUID.randomUUID();
    UUID deliveryId = UUID.randomUUID();
    UUID startCompanyId = UUID.randomUUID();
    UUID endCompanyId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();

    @Mock
    private OrderCommandRepository orderCommandRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

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
                    startCompanyId
            );

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

            verify(orderCommandRepository).save(orderCaptor.capture());
            verify(applicationEventPublisher).publishEvent(
                    new OrderCreatedEvent(
                            orderId,
                            deliveryId,
                            productId,
                            10
                    )
            );

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

    @Nested
    @DisplayName("주문 수정")
    class order_update {
        @Test
        @DisplayName("주문 수정 성공")
        void order_update_success() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );

            OrderUpdateCommand orderUpdateCommand = new OrderUpdateCommand(
                    orderId,
                    70,
                    "오전까지 납품"
            );

            given(orderCommandRepository.save(order)).willReturn(order);

            OrderUpdateResult orderUpdateResult = orderCommandService.updateOrder(
                    order,
                    orderUpdateCommand
            );

            assertThat(order.getQuantity()).isEqualTo(70);
            assertThat(order.getRequest()).isEqualTo("오전까지 납품");
            assertThat(orderUpdateResult.orderId()).isEqualTo(orderId);

            verify(orderCommandRepository).save(order);
        }

        @Test
        @DisplayName("수량이 null이면 기존 수량 유지")
        void order_update_null() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );

            OrderUpdateCommand orderUpdateCommand = new OrderUpdateCommand(
                    orderId,
                    null,
                    "요청사항만 변경"
            );

            given(orderCommandRepository.save(order)).willReturn(order);

            orderCommandService.updateOrder(
                    order,
                    orderUpdateCommand
            );

            assertThat(order.getQuantity()).isEqualTo(100);
            assertThat(order.getRequest()).isEqualTo("요청사항만 변경");
        }
    }

    @Nested
    @DisplayName("주문 삭제")
    class order_delete {
        @Test
        @DisplayName("주문 삭제 성공")
        void order_delete_success() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );

            given(orderCommandRepository.save(order)).willReturn(order);

            orderCommandService.deleteOrder(order);

            verify(orderCommandRepository).save(order);
        }

        @Test
        @DisplayName("존재하지 않는 주문이면 예외")
        void order_delete_not_found() {
            given(orderCommandRepository.findById(orderId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> orderCommandService.findOrderForDelete(orderId))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class order_cancel {
        @Test
        @DisplayName("주문 취소 성공")
        void order_cancel_success() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );
            given(orderCommandRepository.save(order)).willReturn(order);

            OrderCancelResult orderCancelResult = orderCommandService.cancelOrder(order);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);

            assertThat(orderCancelResult.orderId()).isEqualTo(orderId);
            assertThat(orderCancelResult.status()).isEqualTo(OrderStatus.CANCELED);

            verify(orderCommandRepository).save(order);
        }

        @Test
        @DisplayName("이미 취소된 주문 취소 시 예외")
        void order_cancel_already() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "오후까지 납품"
            );
            order.cancel();

            given(orderCommandRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderCommandService.findOrderForCancel(orderId))
                    .isInstanceOf(CustomException.class);
        }
    }
}