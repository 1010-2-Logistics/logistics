package com.logistics.hub.infrastructure.messaging;

import com.logistics.hub.application.event.HubCreatedEvent;
import com.logistics.hub.application.event.HubDeletedEvent;
import com.logistics.hub.application.port.EventPublisher;
import com.logistics.hub.infrastructure.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

// port/EventPublisher.java 구현체. 지금은 로그만 남기고, RabbitMQ 붙이면 실제 발행 코드로 교체하세요.
@Component
@RequiredArgsConstructor
public class HubEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(HubCreatedEvent event) {
        // TODO: 생성 이벤트 구현 시 사용
    }

    @Override
    public void publish(HubDeletedEvent event) {
        // RabbitMQ Exchange로 메시지 전송 (네트워크 타고 타 서비스로 전달)
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                event
        );
    }
}
