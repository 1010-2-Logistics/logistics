package com.logistics.order.application.port;


import com.logistics.order.application.event.OrderCreatedEvent;

public interface EventPublisher {

    void publish(OrderCreatedEvent event);
}
