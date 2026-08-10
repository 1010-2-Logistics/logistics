package com.logistics.order.infrastructure.adapter;

import com.logistics.order.application.dto.result.DeliveryCreateResult;
import com.logistics.order.application.port.DeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DeliveryClientAdapter implements DeliveryPort{
    @Override
    public DeliveryCreateResult createDelivery(
            UUID orderId,
            UUID startHubId,
            UUID endHubId,
            String deliveryAddress,
            String receiverName,
            String receiverSlackId
    ) {
        return null;
    }

    @Override
    public void cancelDelivery(
            UUID orderId
    ) {

    }
}
