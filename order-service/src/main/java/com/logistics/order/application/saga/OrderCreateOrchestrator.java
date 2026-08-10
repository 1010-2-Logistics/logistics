package com.logistics.order.application.saga;


import com.logistics.order.application.saga.command.OrderCreateSagaCommand;
import com.logistics.order.application.dto.result.DeliveryCreateResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.application.port.InventoryPort;
import com.logistics.order.application.service.OrderCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    public OrderCreateResult execute(
            OrderCreateSagaCommand orderCreateSagaCommand
    ) {
        UUID orderId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();

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

        return createOrderWithCompensation(
                operationId,
                orderId,
                orderCreateSagaCommand,
                deliveryCreateResult
        );
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
            DeliveryCreateResult  deliveryCreateResult
    ) {
        try {
            return orderCommandService.createOrder(
                    orderCreateSagaCommand.orderCommand(),
                    orderId,
                    deliveryCreateResult.deliveryId(),
                    orderCreateSagaCommand.startCompanyId()
            );

        } catch (RuntimeException originalException) {

            try {
                deliveryPort.cancelDelivery(orderId);
            } catch (RuntimeException compensationException) {
                log.error(
                        "[ERROR Order] 주문 저장 실패 보상 중 배송 취소 실패. orderId={}, perationId={}",
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
