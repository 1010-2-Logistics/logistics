package com.logistics.order.application.port;

import com.logistics.order.application.dto.result.DeliveryCreateResult;
import com.logistics.order.application.dto.result.DeliveryGetResult;

import java.util.UUID;

public interface DeliveryPort {
    DeliveryCreateResult createDelivery(
            UUID orderId,
            UUID startCompanyId,
            UUID endCompanyId,
            UUID startHubId,
            UUID endHubId,
            String deliveryAddress,
            String receiverName,
            String receiverSlackId
    );
    DeliveryGetResult getDelivery(UUID deliveryId);

    void cancelDelivery(UUID orderId);
}
