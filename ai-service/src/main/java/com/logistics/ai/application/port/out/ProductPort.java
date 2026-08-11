package com.logistics.ai.application.port.out;

import java.util.UUID;

import com.logistics.ai.application.dto.internal.ProductInfo;

public interface ProductPort {

	ProductInfo getProduct(UUID productId);
}
