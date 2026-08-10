package com.logistics.hub.application.port;

import com.logistics.hub.application.event.HubCreatedEvent;
import com.logistics.hub.application.event.HubDeletedEvent;

// 아웃바운드 포트 인터페이스. 구현체는 infrastructure/messaging에 둡니다.
public interface EventPublisher {

    void publish(HubCreatedEvent event);

    void publish(HubDeletedEvent event);
}
