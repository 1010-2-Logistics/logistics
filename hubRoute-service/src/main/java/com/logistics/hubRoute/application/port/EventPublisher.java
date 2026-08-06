package com.logistics.hubRoute.application.port;

import com.logistics.hubRoute.application.event.HubRouteCreatedEvent;

// 아웃바운드 포트 인터페이스. 구현체는 infrastructure/messaging에 둡니다.
public interface EventPublisher {

    void publish(HubRouteCreatedEvent event);
}
