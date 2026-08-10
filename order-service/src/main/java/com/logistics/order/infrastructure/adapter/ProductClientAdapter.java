package com.logistics.order.infrastructure.adapter;


import com.logistics.order.application.dto.result.ProductGetResult;
import com.logistics.order.application.port.ProductPort;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import com.logistics.order.infrastructure.feign.client.ProductClient;
import com.logistics.order.infrastructure.feign.response.ProductGetResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductPort {
    private final ProductClient productClient;

    @Override
    public ProductGetResult getProduct(
            UUID productId
    ) {
        try {
            ProductGetResponse productGetResponse = productClient.getProduct(productId).getData();

            return new ProductGetResult(
                    productGetResponse.productId(),
                    productGetResponse.companyId(),
                    productGetResponse.productName()
            );
        } catch (FeignException.NotFound e) {
            throw new CustomException(
                    OrderErrorCode.ORDER_REFERENCE_NOT_FOUND
            );
        } catch (FeignException e) {
            throw new CustomException(
                    OrderErrorCode.ORDER_SERVICE_UNAVAILABLE
            );
        }
    }
}
