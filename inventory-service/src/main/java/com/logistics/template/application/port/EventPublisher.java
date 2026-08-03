package com.logistics.template.application.port;

import com.logistics.template.application.event.InventoryCreatedEvent;

// 아웃바운드 포트 인터페이스. 구현체는 infrastructure/messaging에 둡니다.
public interface EventPublisher {

    void publish(InventoryCreatedEvent event);
}
