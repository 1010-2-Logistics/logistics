package com.logistics.template.infrastructure.messaging;

import com.logistics.template.application.event.InventoryCreatedEvent;
import com.logistics.template.application.port.EventPublisher;
import org.springframework.stereotype.Component;

// port/EventPublisher.java 구현체. 지금은 로그만 남기고, RabbitMQ 붙이면 실제 발행 코드로 교체하세요.
@Component
public class SampleEventPublisher implements EventPublisher {

    @Override
    public void publish(InventoryCreatedEvent event) {
        // TODO: RabbitTemplate 등으로 실제 메시지 발행
    }
}
