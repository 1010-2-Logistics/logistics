package com.logistics.inventory.infrastructure.adapter;

import com.logistics.inventory.application.dto.internal.response.ProductExistsResponseDto;
import com.logistics.inventory.application.port.ProductPort;
import com.logistics.inventory.infrastructure.feign.client.ProductClient;
import com.logistics.inventory.infrastructure.feign.response.ProductValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductPort {
    private final ProductClient productClient;

    @Override
    public ProductExistsResponseDto getProduct(UUID productId) {
        ProductValidationResponse productValidationResponse = productClient.getProduct(productId).getData();

        return new ProductExistsResponseDto(
                productValidationResponse.productId(),
                productValidationResponse.exists()
        );
    }
}
