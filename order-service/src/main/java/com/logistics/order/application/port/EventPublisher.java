package com.logistics.order.application.port;


import com.logistics.order.application.event.OrderCreatedEvent;

import java.util.UUID;

public interface EventPublisher {
    boolean publish(
            OrderCreatedEvent orderCreatedEvent,
            UUID eventId
    );

    void publish(OrderCreatedEvent event);
}
