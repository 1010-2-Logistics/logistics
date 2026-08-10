package com.logistics.order.application.saga;


import com.logistics.order.application.dto.command.OrderCreateSagaCommand;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.infrastructure.feign.client.DeliveryClient;
import com.logistics.order.infrastructure.feign.client.InventoryClient;
import com.logistics.order.infrastructure.feign.request.DeliveryCreateRequest;
import com.logistics.order.infrastructure.feign.request.InventoryDeductionRequest;
import com.logistics.order.infrastructure.feign.request.InventoryRestorationRequest;
import com.logistics.order.infrastructure.feign.response.DeliveryCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateOrchestrator {

    // 1. application에 넣은 판단 기준 :
    // 여러 작업의 실행 순서를 조율하는 애플리케이션 로직이기 때문

    // 2. 변경 작업
    // inventory : 재고 차감/복원
    // delivery : 배송 생성/취소
    // order : 주문 저장
    private final OrderCommandService orderCommandService;
    private final InventoryClient inventoryClient;
    private final DeliveryClient deliveryClient;

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

        DeliveryCreateResponse deliveryCreateResponse = createDeliveryWithCompensation(
                operationId,
                orderId,
                orderCreateSagaCommand
        );

        return createOrderWithCompensation(
                operationId,
                orderId,
                orderCreateSagaCommand,
                deliveryCreateResponse
        );
    }

    private void deductInventory(
            UUID operationId,
            UUID orderId,
            OrderCreateSagaCommand orderCreateSagaCommand
    ) {
        InventoryDeductionRequest inventoryDeductionRequest = new InventoryDeductionRequest(
                operationId,
                orderId,
                orderCreateSagaCommand.orderCommand().productId(),
                orderCreateSagaCommand.startHubId(),
                orderCreateSagaCommand.orderCommand().quantity()
        );

        inventoryClient.deductInventory(inventoryDeductionRequest);
    }

    private DeliveryCreateResponse createDeliveryWithCompensation(
            UUID operationId,
            UUID orderId,
            OrderCreateSagaCommand orderCreateSagaCommand
    ) {
        DeliveryCreateRequest deliveryCreateRequest = new DeliveryCreateRequest(
                orderId,
                orderCreateSagaCommand.startHubId(),
                orderCreateSagaCommand.endHubId(),
                orderCreateSagaCommand.endCompanyAddress(),
                orderCreateSagaCommand.receiverName(),
                orderCreateSagaCommand.receiverSlackId()
        );

        try {
            return deliveryClient.createDelivery(deliveryCreateRequest).getData();

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
            DeliveryCreateResponse deliveryCreateResponse
    ) {
        try {
            return orderCommandService.createOrder(
                    orderCreateSagaCommand.orderCommand(),
                    orderId,
                    deliveryCreateResponse.deliveryId(),
                    orderCreateSagaCommand.startCompanyId()
            );

        } catch (RuntimeException originalException) {

            try {
                deliveryClient.cancelDelivery(orderId);
            } catch (RuntimeException compensationException) {
                log.error(
                        "주문 저장 실패 보상 중 배송 취소 실패. orderId={}",
                        orderId,
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
            InventoryRestorationRequest request = new InventoryRestorationRequest(
                    operationId,
                    orderId,
                    orderCreateSagaCommand.orderCommand().productId(),
                    orderCreateSagaCommand.startHubId(),
                    orderCreateSagaCommand.orderCommand().quantity()
            );

            inventoryClient.restoreInventory(request);

        } catch (RuntimeException compensationException) {
            log.error(
                    "주문 생성 보상 중 재고 복원 실패. orderId={}",
                    orderId,
                    compensationException
            );

            originalException.addSuppressed(compensationException);
        }
    }
}
