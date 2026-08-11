package com.logistics.inventory.infrastructure.adapter;

import com.logistics.inventory.application.dto.result.ProductExistsResponseDto;
import com.logistics.inventory.application.port.ProductPort;
import com.logistics.inventory.global.exception.CommonErrorCode;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.infrastructure.feign.client.ProductClient;
import com.logistics.inventory.infrastructure.feign.response.ProductValidationResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductPort {
    private final ProductClient productClient;

    @Override
    public ProductExistsResponseDto getProduct(UUID productId) {
        try {
            ProductValidationResponse productValidationResponse = productClient.getProduct(productId).getData();

            return new ProductExistsResponseDto(
                    productValidationResponse.productId(),
                    productValidationResponse.exists()
            );
        } catch (FeignException e) {
            throw new CustomException(
                    CommonErrorCode.INVENTORY_SERVICE_UNAVAILABLE
            );
        }
    }
}
