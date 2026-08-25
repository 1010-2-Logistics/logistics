package com.logistics.order.infrastructure.messaging;


import com.logistics.order.application.service.OutboxPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxPublishService outboxPublishService;

    // 이전 실행이 끝난 시점부터 1초 뒤에 다음 실행
    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {
        outboxPublishService.publishPendingEvents();
    }
}
