package com.logistics.hubRoute.application.event;

import com.logistics.hubRoute.application.service.HubRouteCommandService;
import com.logistics.hubRoute.infrastructure.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class HubRouteEventListener {

    private final HubRouteCommandService hubRouteCommandService;

    // RabbitMQ 큐를 컨슘하는 DTO 레코드 (메시지 수신용)
    public record HubDeletedMessage(UUID hubId, Long deletedBy) {}

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleHubDeleted(HubDeletedMessage message) {
        System.out.println(">>> [RabbitMQ 수신 완료] 삭제된 Hub ID: " + message.hubId());
        hubRouteCommandService.deleteHubRoutesByHubId(message.hubId(), message.deletedBy());
    }
}