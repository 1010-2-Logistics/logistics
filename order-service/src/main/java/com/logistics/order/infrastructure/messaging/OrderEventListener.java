package com.logistics.order.infrastructure.messaging;


import com.logistics.order.application.event.OrderCreatedEvent;
import com.logistics.order.application.port.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


// 주문 생성 트랜잭션이 커밋된 후 RabbitMQ 이벤트를 발행한다

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final EventPublisher eventPublisher;

    // slackCommandService에 있는 유틸 친구들 이용해서 짠 메서드가.. 아래 어노테이션 한 줄로 대체 가능함
    // 명시적으로 AFTER_COMMIT 표시
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCreatedEvent orderCreatedEvent) {
        log.info("주문 생성 이벤트 수신. orderId={}", orderCreatedEvent.orderId());
        try {
            eventPublisher.publish(orderCreatedEvent);
            log.info("주문 생성 이벤트 발행 완료. orderId={}", orderCreatedEvent.orderId());
        } catch (Exception e) {
            log.error("주문 생성 이벤트 발행 실패. orderId={}", orderCreatedEvent.orderId(), e);
            throw e;
        }
    }
}
