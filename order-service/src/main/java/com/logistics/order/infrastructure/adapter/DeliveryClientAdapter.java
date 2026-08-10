package com.logistics.order.infrastructure.adapter;

import com.logistics.order.application.dto.result.DeliveryCreateResult;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import com.logistics.order.infrastructure.feign.client.DeliveryClient;
import com.logistics.order.infrastructure.feign.request.DeliveryCreateRequest;
import com.logistics.order.infrastructure.feign.response.DeliveryCreateResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DeliveryClientAdapter implements DeliveryPort {
    private final DeliveryClient deliveryClient;

    @Override
    public DeliveryCreateResult createDelivery(
            UUID orderId,
            UUID startHubId,
            UUID endHubId,
            String deliveryAddress,
            String receiverName,
            String receiverSlackId
    ) {
        try {
            DeliveryCreateRequest deliveryCreateRequest = new DeliveryCreateRequest(
                    orderId,
                    startHubId,
                    endHubId,
                    deliveryAddress,
                    receiverName,
                    receiverSlackId
            );

            DeliveryCreateResponse deliveryCreateResponse = deliveryClient.createDelivery(deliveryCreateRequest).getData();

            return new DeliveryCreateResult(
                    deliveryCreateResponse.deliveryId(),
                    deliveryCreateResponse.routeCount()
            );
        } catch (FeignException e) {
            throw new CustomException(
                    OrderErrorCode.ORDER_SERVICE_UNAVAILABLE
            );
        }
    }

    @Override
    public void cancelDelivery(
            UUID orderId
    ) {
        deliveryClient.cancelDelivery(orderId);
    }
}
