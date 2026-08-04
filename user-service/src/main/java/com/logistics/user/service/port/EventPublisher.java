package com.logistics.user.service.port;

import com.logistics.user.service.event.UserCreatedEvent;

// 아웃바운드 포트 인터페이스. 구현체는 infrastructure/messaging에 둡니다.
public interface EventPublisher {

    void publish(UserCreatedEvent event);
}
