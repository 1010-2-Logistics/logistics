package com.logistics.order.infrastructure.adapter;

import com.logistics.order.application.dto.result.DeliveryCreateResult;
import com.logistics.order.application.dto.result.DeliveryGetResult;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import com.logistics.order.infrastructure.feign.client.DeliveryClient;
import com.logistics.order.infrastructure.feign.request.DeliveryCreateRequest;
import com.logistics.order.infrastructure.feign.response.DeliveryCreateResponse;
import com.logistics.order.infrastructure.feign.response.DeliveryResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryClientAdapter implements DeliveryPort {
    private final DeliveryClient deliveryClient;

    @Override
    public DeliveryCreateResult createDelivery(
            UUID orderId,
            UUID startCompanyId,
            UUID endCompanyId,
            UUID startHubId,
            UUID endHubId,
            String deliveryAddress,
            String receiverName,
            String receiverSlackId
    ) {
        try {
            DeliveryCreateRequest deliveryCreateRequest = new DeliveryCreateRequest(
                    orderId,
                    startCompanyId,
                    endCompanyId,
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
            log.error(
                    "Delivery service 호출 실패. status={}, response={}",
                    e.status(),
                    e.contentUTF8(),
                    e
            );
            throw new CustomException(
                    OrderErrorCode.ORDER_SERVICE_UNAVAILABLE
            );
        }
    }

    @Override
    public DeliveryGetResult getDelivery(
            UUID deliveryId
    ) {
        try {
            DeliveryResponse deliveryResponse = deliveryClient.getDelivery(deliveryId).getData();

            return new DeliveryGetResult(
                    deliveryResponse.deliveryId(),
                    deliveryResponse.startHubId(),
                    deliveryResponse.endHubId(),
                    deliveryResponse.deliveryManagerId()
            );
        } catch (FeignException e) {
            log.error(
                    "Delivery service 호출 실패. status={}, response={}",
                    e.status(),
                    e.contentUTF8(),
                    e
            );

            throw new CustomException(
                    OrderErrorCode.ORDER_SERVICE_UNAVAILABLE
            );
        }
    }

    @Override
    public void cancelDelivery(
            UUID orderId
    ) {
        try {
            deliveryClient.cancelDelivery(orderId);

        } catch (FeignException.NotFound e) {
            throw new CustomException(
                    OrderErrorCode.ORDER_NOT_FOUND
            );

        } catch (FeignException.Conflict e) {
            throw new CustomException(
                    OrderErrorCode.ORDER_CANCEL_CONFLICT
            );

        } catch (FeignException e) {
            log.error(
                    "Delivery service 호출 실패. status={}, response={}",
                    e.status(),
                    e.contentUTF8(),
                    e
            );
            throw new CustomException(
                    OrderErrorCode.ORDER_SERVICE_UNAVAILABLE
            );
        }
    }
}
