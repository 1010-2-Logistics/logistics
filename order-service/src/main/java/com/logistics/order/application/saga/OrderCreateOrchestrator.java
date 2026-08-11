package com.logistics.order.application.saga;


import com.logistics.order.application.dto.result.DeliveryCreateResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.application.port.IdempotencyPort;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.saga.command.OrderCreateSagaCommand;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateOrchestrator {
    // application에 OrderCreateOrchestrator를 넣은 판단 기준 :
    // 여러 작업의 실행 순서를 조율하는 애플리케이션 로직이기 때문

    private final OrderCommandService orderCommandService;
    private final InventoryPort inventoryPort;
    private final DeliveryPort deliveryPort;

    private final IdempotencyPort idempotencyPort;

    // TroubleShooting01 - 멱등 키 : orderId를 사용하게 되면 다른 업무인데 같은 업무로 취급받아서 operationId와 구분했던 문제
    // idempotencyKey로 교체하면서 든 생각
    // 첫 번째 실행에서 이미 성공 결과가 있어서 차감 안 하고 이전 결과를 줘버린다
    // 그런데 실제 재고는 첫 요청 실패 때 복원해버린다
    // Inventory의 멱등 기록 : 차감했음
    // 실제 Inventory stock : 보상해서 원상복구됨

    // 따라서 역할을 나눠야 한다
    // idempotencyKey : HTTP 주문 생성 요청 전체를 식별, 같은 요청 재시도인지 판단
    // operationId : 그 요청 안에서 실행되는 이번 재고 작업을 식별

    public OrderCreateResult execute(
            OrderCreateSagaCommand orderCreateSagaCommand
    ) {
        UUID idempotencyKey = orderCreateSagaCommand.idempotencyKey();

        String key = "order:create:" + idempotencyKey;
        Duration ttl = Duration.ofMinutes(10);

        // 같은 주문 생성 요청이 전에 성공했는지 확인
        Optional<OrderCreateResult> previousResult = idempotencyPort.getResult(
                key,
                OrderCreateResult.class
        );
        if (previousResult.isPresent()) {
            return previousResult.get();
        }

        // 동일 요청 동시 실행 방지
        boolean acquired = idempotencyPort.acquire(
                key,
                ttl
        );
        if (!acquired) {
            throw new CustomException(
                    OrderErrorCode.ORDER_ALREADY_PROCESSING
            );
        }

        // 이번 Saga 실행 및 주문 생성을 위한 식별자 생성
        UUID operationId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        try {
            deductInventory(
                    operationId,
                    orderId,
                    orderCreateSagaCommand
            );

            DeliveryCreateResult deliveryCreateResult = createDeliveryWithCompensation(
                    operationId,
                    orderId,
                    orderCreateSagaCommand
            );

            OrderCreateResult orderCreateResult = createOrderWithCompensation(
                    operationId,
                    orderId,
                    orderCreateSagaCommand,
                    deliveryCreateResult
            );

            idempotencyPort.complete(
                    key,
                    orderCreateResult,
                    ttl
            );

            return orderCreateResult;

        } catch (RuntimeException e) {
            idempotencyPort.release(key);
            throw e;
        }
    }

    private void deductInventory(
            UUID operationId,
            UUID orderId,
            OrderCreateSagaCommand orderCreateSagaCommand
    ) {
        inventoryPort.deductInventory(
                operationId,
                orderId,
                orderCreateSagaCommand.orderCommand().productId(),
                orderCreateSagaCommand.startHubId(),
                orderCreateSagaCommand.orderCommand().quantity()
        );
    }

    private DeliveryCreateResult createDeliveryWithCompensation(
            UUID operationId,
            UUID orderId,
            OrderCreateSagaCommand orderCreateSagaCommand
    ) {
        try {
            return deliveryPort.createDelivery(
                    orderId,
                    orderCreateSagaCommand.startCompanyId(),
                    orderCreateSagaCommand.orderCommand().endCompanyId(),
                    orderCreateSagaCommand.startHubId(),
                    orderCreateSagaCommand.endHubId(),
                    orderCreateSagaCommand.endCompanyAddress(),
                    orderCreateSagaCommand.receiverName(),
                    orderCreateSagaCommand.receiverSlackId()
            );

        } catch (RuntimeException originalException) {
            compensateInventoryRestoration(
                    operationId,
                    orderId,
                    orderCreateSagaCommand,
                    originalException
            );

            throw originalException;
        }
    }

    private OrderCreateResult createOrderWithCompensation(
            UUID operationId,
            UUID orderId,
            OrderCreateSagaCommand orderCreateSagaCommand,
            DeliveryCreateResult deliveryCreateResult
    ) {
        try {
            return orderCommandService.createOrder(
                    orderCreateSagaCommand.orderCommand(),
                    orderId,
                    deliveryCreateResult.deliveryId(),
                    orderCreateSagaCommand.startCompanyId(),
                    orderCreateSagaCommand.receiverName(),
                    orderCreateSagaCommand.receiverSlackId()
            );

        } catch (RuntimeException originalException) {

            try {
                deliveryPort.cancelDelivery(orderId);
            } catch (RuntimeException compensationException) {
                log.error(
                        "[ERROR Order] 주문 저장 실패 보상 중 배송 취소 실패. orderId={}, operationId={}",
                        orderId,
                        operationId,
                        compensationException
                );

                originalException.addSuppressed(compensationException);
            }

            compensateInventoryRestoration(
                    operationId,
                    orderId,
                    orderCreateSagaCommand,
                    originalException
            );

            throw originalException;
        }
    }

    private void compensateInventoryRestoration(
            UUID operationId,
            UUID orderId,
            OrderCreateSagaCommand orderCreateSagaCommand,
            RuntimeException originalException
    ) {
        try {
            inventoryPort.restoreInventory(
                    operationId,
                    orderId,
                    orderCreateSagaCommand.orderCommand().productId(),
                    orderCreateSagaCommand.startHubId(),
                    orderCreateSagaCommand.orderCommand().quantity()
            );

        } catch (RuntimeException compensationException) {
            log.error(
                    "[ERROR Order] 주문 생성 보상 중 재고 복원 실패. orderId={}, operationId={}",
                    orderId,
                    operationId,
                    compensationException
            );

            originalException.addSuppressed(compensationException);
        }
    }
}
