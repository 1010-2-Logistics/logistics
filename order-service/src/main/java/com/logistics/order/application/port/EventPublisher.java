package com.logistics.order.application.port;


import com.logistics.order.application.event.OrderCreatedEvent;

// 아웃바운드 포트 인터페이스. 구현체는 infrastructure/messaging에 둡니다.
public interface EventPublisher {

    void publish(OrderCreatedEvent event);
}
