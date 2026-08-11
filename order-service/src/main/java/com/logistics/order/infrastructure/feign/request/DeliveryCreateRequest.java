package com.logistics.order.infrastructure.feign.request;

import java.util.UUID;

public record DeliveryCreateRequest(
        UUID orderId,
        UUID startCompanyId,
        UUID endCompanyId,
        UUID startHubId,
        UUID endHubId,
        String deliveryAddress,
        String receiverName,
        String receiverSlackId
) {
}
