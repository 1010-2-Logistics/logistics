package com.logistics.order.application.saga;

import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.result.DeliveryCreateResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.application.port.IdempotencyPort;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.saga.command.OrderCreateSagaCommand;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCreateOrchestratorTest {
    UUID endCompanyId = UUID.randomUUID();
    UUID startCompanyId = UUID.randomUUID();
    UUID startHubId = UUID.randomUUID();
    UUID endHubId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();

    @Mock
    private OrderCommandService orderCommandService;

    @Mock
    private InventoryPort inventoryPort;

    @Mock
    private DeliveryPort deliveryPort;

    @Mock
    private IdempotencyPort idempotencyPort;

    @InjectMocks
    private OrderCreateOrchestrator orderCreateOrchestrator;

    @Test
    @DisplayName("배송 생성 실패 시 재고 복원")
    void create_order_delivery_fail_restore_inventory() {
        UUID idempotencyKey = UUID.randomUUID();

        OrderCreateCommand orderCommand = new OrderCreateCommand(endCompanyId, productId, 100, "request");

        OrderCreateSagaCommand command = new OrderCreateSagaCommand(
                orderCommand,
                startCompanyId,
                startHubId,
                endHubId,
                "address",
                "name",
                "slack",
                idempotencyKey
        );

        given(idempotencyPort.getResult(anyString(), eq(OrderCreateResult.class))).willReturn(Optional.empty());
        given(idempotencyPort.acquire(anyString(), any(Duration.class))).willReturn(true);

        doThrow(new RuntimeException("배송 실패"))
                .when(deliveryPort)
                .createDelivery(
                        any(UUID.class),
                        eq(startCompanyId),
                        eq(endCompanyId),
                        eq(startHubId),
                        eq(endHubId),
                        eq("address"),
                        eq("name"),
                        eq("slack")
                );

        assertThatThrownBy(() -> orderCreateOrchestrator.execute(command))
                .isInstanceOf(RuntimeException.class);

        verify(inventoryPort).restoreInventory(
                any(UUID.class),
                any(UUID.class),
                eq(productId),
                eq(startHubId),
                eq(100)
        );
    }

    @Test
    @DisplayName("주문 저장 실패 시 배송 취소 후 재고 복원")
    void create_order_save_fail_compensate() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        OrderCreateCommand orderCommand = new OrderCreateCommand(
                endCompanyId,
                productId,
                100,
                "request"
        );
        OrderCreateSagaCommand command = new OrderCreateSagaCommand(
                orderCommand,
                startCompanyId,
                startHubId,
                endHubId,
                "address",
                "name",
                "slack",
                idempotencyKey
        );

        DeliveryCreateResult deliveryResult = mock(DeliveryCreateResult.class);
        given(idempotencyPort.getResult(anyString(), eq(OrderCreateResult.class))).willReturn(Optional.empty());
        given(idempotencyPort.acquire(anyString(), any(Duration.class))).willReturn(true);
        given(deliveryPort.createDelivery(
                any(UUID.class),
                eq(startCompanyId),
                eq(endCompanyId),
                eq(startHubId),
                eq(endHubId),
                eq("address"),
                eq("name"),
                eq("slack")
        )).willReturn(deliveryResult);

        given(deliveryResult.deliveryId()).willReturn(deliveryId);

        given(orderCommandService.createOrder(
                eq(orderCommand),
                any(UUID.class),
                eq(deliveryId),
                eq(startCompanyId),
                eq("name"),
                eq("slack")
        )).willThrow(new RuntimeException("DB 저장 실패"));

        assertThatThrownBy(() -> orderCreateOrchestrator.execute(command))
                .isInstanceOf(RuntimeException.class);

        verify(deliveryPort).cancelDelivery(any(UUID.class));

        verify(inventoryPort).restoreInventory(
                any(UUID.class),
                any(UUID.class),
                eq(productId),
                eq(startHubId),
                eq(100)
        );
    }

    @Test
    @DisplayName("동일 멱등키의 성공 결과가 존재하면 Saga를 재실행하지 않고 기존 결과 반환")
    void create_order_idempotent_result() {
        UUID idempotencyKey = UUID.randomUUID();
        String key = "order:create:" + idempotencyKey;

        OrderCreateSagaCommand orderCreateSagaCommand = mock(OrderCreateSagaCommand.class);

        OrderCreateResult previousResult = mock(OrderCreateResult.class);

        given(orderCreateSagaCommand.idempotencyKey()).willReturn(idempotencyKey);

        given(idempotencyPort.getResult(
                key,
                OrderCreateResult.class
        )).willReturn(Optional.of(previousResult));

        OrderCreateResult result = orderCreateOrchestrator.execute(orderCreateSagaCommand);

        assertThat(result).isSameAs(previousResult);

        verify(idempotencyPort).getResult(
                key,
                OrderCreateResult.class
        );

        verifyNoInteractions(
                inventoryPort,
                deliveryPort,
                orderCommandService
        );
    }

    @Test
    @DisplayName("이전 결과가 없고 멱등키 선점에 성공하면 Saga 실행 후 결과 저장")
    void create_order_idempotent_success() {
        UUID idempotencyKey = UUID.randomUUID();
        String key = "order:create:" + idempotencyKey;
        UUID deliveryId = UUID.randomUUID();

        OrderCreateCommand orderCreateCommand = new OrderCreateCommand(
                endCompanyId,
                productId,
                100,
                "오후까지 납품"
        );

        OrderCreateSagaCommand command = new OrderCreateSagaCommand(
                orderCreateCommand,
                startCompanyId,
                startHubId,
                endHubId,
                "서울특별시 송파구",
                "임시 수령인",
                "TEMP_SLACK_ID",
                idempotencyKey
        );

        DeliveryCreateResult deliveryCreateResult = mock(DeliveryCreateResult.class);

        OrderCreateResult orderCreateResult = mock(OrderCreateResult.class);

        given(idempotencyPort.getResult(
                key,
                OrderCreateResult.class
        )).willReturn(Optional.empty());

        given(idempotencyPort.acquire(
                eq(key),
                any(Duration.class)
        )).willReturn(true);

        given(deliveryPort.createDelivery(
                any(UUID.class),
                eq(startCompanyId),
                eq(endCompanyId),
                eq(startHubId),
                eq(endHubId),
                eq("서울특별시 송파구"),
                eq("임시 수령인"),
                eq("TEMP_SLACK_ID")
        )).willReturn(deliveryCreateResult);

        given(deliveryCreateResult.deliveryId()).willReturn(deliveryId);

        given(orderCommandService.createOrder(
                eq(orderCreateCommand),
                any(UUID.class),
                eq(deliveryId),
                eq(startCompanyId),
                eq("임시 수령인"),
                eq("TEMP_SLACK_ID")
        )).willReturn(orderCreateResult);

        OrderCreateResult result = orderCreateOrchestrator.execute(command);

        assertThat(result).isSameAs(orderCreateResult);

        verify(idempotencyPort).complete(
                eq(key),
                same(orderCreateResult),
                any(Duration.class)
        );
    }

    @Test
    @DisplayName("동일 멱등키가 처리 중이면 주문 생성 중단")
    void create_order_idempotent_processing() {
        UUID idempotencyKey = UUID.randomUUID();
        String key = "order:create:" + idempotencyKey;
        OrderCreateSagaCommand command = mock(OrderCreateSagaCommand.class);

        given(command.idempotencyKey()).willReturn(idempotencyKey);
        given(idempotencyPort.getResult(key, OrderCreateResult.class)).willReturn(Optional.empty());
        given(idempotencyPort.acquire(eq(key), any(Duration.class))).willReturn(false);

        assertThatThrownBy(() -> orderCreateOrchestrator.execute(command))
                .isInstanceOf(CustomException.class);

        verifyNoInteractions(
                inventoryPort,
                deliveryPort,
                orderCommandService
        );
    }

    @Test
    @DisplayName("Saga 실행 중 실패하면 멱등키 해제")
    void create_order_idempotent_release_on_failure() {
        UUID idempotencyKey = UUID.randomUUID();
        String key = "order:create:" + idempotencyKey;
        OrderCreateCommand orderCreateCommand = new OrderCreateCommand(
                endCompanyId,
                productId,
                100,
                "오후까지 납품"
        );
        OrderCreateSagaCommand command = new OrderCreateSagaCommand(
                orderCreateCommand,
                startCompanyId,
                startHubId,
                endHubId,
                "서울특별시 송파구",
                "임시 수령인",
                "TEMP_SLACK_ID",
                idempotencyKey
        );

        given(idempotencyPort.getResult(key, OrderCreateResult.class)).willReturn(Optional.empty());
        given(idempotencyPort.acquire(eq(key), any(Duration.class))).willReturn(true);

        // doThrow().when() : 순서 뒤집기
        doThrow(new RuntimeException("재고 차감 실패"))
                .when(inventoryPort)
                .deductInventory(
                        any(UUID.class),
                        any(UUID.class),
                        eq(productId),
                        eq(startHubId),
                        eq(100)
                );

        assertThatThrownBy(() -> orderCreateOrchestrator.execute(command))
                .isInstanceOf(RuntimeException.class);

        verify(idempotencyPort).release(key);
    }
}