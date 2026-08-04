package com.logistics.user.infrastructure.messaging;

import com.logistics.user.service.event.UserCreatedEvent;
import com.logistics.user.service.port.EventPublisher;
import org.springframework.stereotype.Component;

// port/EventPublisher.java 구현체. 지금은 로그만 남기고, RabbitMQ 붙이면 실제 발행 코드로 교체하세요.
@Component
public class UserEventPublisher implements EventPublisher {

    @Override
    public void publish(UserCreatedEvent event) {
        // TODO: Rabbituser 등으로 실제 메시지 발행
    }
}
