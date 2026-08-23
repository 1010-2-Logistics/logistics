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
import com.logistics.order.domain.entity.OrderStatus;
import com.logistics.order.domain.entity.OutboxEvent;
import com.logistics.order.domain.entity.OutboxStatus;
import com.logistics.order.domain.repository.OrderCommandRepository;
import com.logistics.order.domain.repository.OutboxRepository;
import com.logistics.order.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderCommandService orderCommandService;

    @Test
    @DisplayName("이미 취소된 주문은 수정할 수 없음")
    void order_update_canceled() {
        Order order = Order.create(
                orderId,
                deliveryId,
                startCompanyId,
                endCompanyId,
                productId,
                100,
                "요청"
        );
        order.cancel();

        given(orderCommandRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderCommandService.findOrderForUpdate(orderId))
                .isInstanceOf(CustomException.class);
    }

    @Nested
    @DisplayName("주문 생성")
    class order_create {
        @Test
        @DisplayName("주문 생성 성공 시 주문과 PENDING Outbox 이벤트를 함께 저장")
        void createOrder_success_saveOrderAndOutbox() throws Exception {
            UUID orderId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID startCompanyId = UUID.randomUUID();
            UUID endCompanyId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            OrderCreateCommand orderCreateCommand = new OrderCreateCommand(
                    endCompanyId,
                    productId,
                    10,
                    "8월 6일 오전까지 납품"
            );

            given(orderCommandRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(objectMapper.writeValueAsString(any(OrderCreatedEvent.class))).willReturn("{\"orderId\":\"" + orderId + "\"}");

            OrderCreateResult result = orderCommandService.createOrder(
                    orderCreateCommand,
                    orderId,
                    deliveryId,
                    startCompanyId,
                    "receiverName",
                    "receiverSlackId"
            );

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

            verify(orderCommandRepository).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();

            assertThat(savedOrder.getOrderId()).isEqualTo(orderId);
            assertThat(savedOrder.getDeliveryId()).isEqualTo(deliveryId);
            assertThat(savedOrder.getStartCompanyId()).isEqualTo(startCompanyId);
            assertThat(savedOrder.getEndCompanyId()).isEqualTo(endCompanyId);
            assertThat(savedOrder.getProductId()).isEqualTo(productId);
            assertThat(savedOrder.getQuantity()).isEqualTo(10);
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);

            //Outbox 저장 확인
            ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxRepository).save(outboxCaptor.capture());
            OutboxEvent savedOutbox = outboxCaptor.getValue();

            assertThat(savedOutbox.getEventId()).isNotNull();
            assertThat(savedOutbox.getAggregateId()).isEqualTo(orderId);
            assertThat(savedOutbox.getEventType()).isEqualTo("ORDER_CREATED");
            assertThat(savedOutbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(savedOutbox.getPayload()).contains(orderId.toString());
            assertThat(savedOutbox.getCreatedAt()).isNotNull();
            assertThat(savedOutbox.getPublishedAt()).isNull();

            assertThat(result.orderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Outbox 이벤트 직렬화 실패 시 주문 생성 실패 처리")
        void createOrder_fail_whenOutboxSerializationFails() throws Exception {
            UUID orderId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID startCompanyId = UUID.randomUUID();
            UUID endCompanyId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            OrderCreateCommand orderCreateCommand = new OrderCreateCommand(
                    endCompanyId,
                    productId,
                    10,
                    "요청사항"
            );

            given(orderCommandRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // {} : 정확히 어떤 OrderCreatedEvent 객체인지는 상관없고, OrderCreatedEvent 타입이면 전부 매칭해~
            // ObjectMapper가 어떤 OrderCreatedEvent든 JSON으로 직렬화하려고 하면,
            // 테스트에서는 강제로 JsonProcessingException을 발생시켜라
            given(objectMapper.writeValueAsString(any(OrderCreatedEvent.class))).willThrow(new JsonProcessingException("직렬화 실패") {
            });

            assertThatThrownBy(() ->
                    orderCommandService.createOrder(
                            orderCreateCommand,
                            orderId,
                            deliveryId,
                            startCompanyId,
                            "receiverName",
                            "receiverSlackId"
                    ))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("주문 생성 이벤트 직렬화에 실패했습니다.");

            verify(outboxRepository, never()).save(any(OutboxEvent.class));
        }
    }

    @Nested
    @DisplayName("주문 수정")
    class order_update {
        @Test
        @DisplayName("수정할 주문이 없으면 예외")
        void order_update_not_found() {
            given(orderCommandRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderCommandService.findOrderForUpdate(orderId))
                    .isInstanceOf(CustomException.class);
        }

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
        @DisplayName("취소할 주문이 없으면 예외")
        void order_cancel_not_found() {
            given(orderCommandRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderCommandService.findOrderForCancel(orderId))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("이미 삭제된 주문이면 예외")
        void order_delete_already_deleted() {
            Order order = Order.create(
                    orderId,
                    deliveryId,
                    startCompanyId,
                    endCompanyId,
                    productId,
                    100,
                    "요청"
            );
            order.delete(1L);

            given(orderCommandRepository.findById(orderId)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderCommandService.findOrderForDelete(orderId))
                    .isInstanceOf(CustomException.class);
        }


        @Test
        @DisplayName("주문 삭제 성공")
        void order_delete_success() {
            Long deletedBy = 1L;
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

            orderCommandService.deleteOrder(
                    order,
                    deletedBy
            );

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