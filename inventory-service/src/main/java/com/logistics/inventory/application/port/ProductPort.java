package com.logistics.inventory.application.port;

import com.logistics.inventory.application.dto.internal.response.ProductExistsResponseDto;

import java.util.UUID;

public interface ProductPort {
    ProductExistsResponseDto getProduct(UUID productId);
}
