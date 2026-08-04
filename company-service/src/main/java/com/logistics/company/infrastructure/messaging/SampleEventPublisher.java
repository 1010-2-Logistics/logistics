package com.logistics.company.infrastructure.messaging;

import org.springframework.stereotype.Component;

import com.logistics.company.application.event.SampleCreatedEvent;
import com.logistics.company.application.port.EventPublisher;

// port/EventPublisher.java 구현체. 지금은 로그만 남기고, RabbitMQ 붙이면 실제 발행 코드로 교체하세요.
@Component
public class SampleEventPublisher implements EventPublisher {

    @Override
    public void publish(SampleCreatedEvent event) {
        // TODO: RabbitTemplate 등으로 실제 메시지 발행
    }
}
