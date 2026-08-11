package com.logistics.order.application.port;

import com.logistics.order.application.dto.result.ProductGetResult;

import java.util.UUID;

public interface ProductPort {
    ProductGetResult getProduct(
            UUID productId
    );
}
