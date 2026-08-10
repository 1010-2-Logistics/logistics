package com.logistics.order.application.saga;


import com.logistics.order.application.dto.command.OrderCreateSagaCommand;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.infrastructure.feign.client.DeliveryClient;
import com.logistics.order.infrastructure.feign.client.InventoryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        //  재고 차감
        // 배송 생성

        // 주문 저장

        // 실패 시 보상

        return null;
    }
}
