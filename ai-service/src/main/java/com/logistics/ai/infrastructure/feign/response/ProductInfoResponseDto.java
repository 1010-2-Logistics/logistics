package com.logistics.ai.infrastructure.feign.response;

import java.util.UUID;

import com.logistics.ai.application.dto.internal.ProductInfo;

public record ProductInfoResponseDto(
		UUID productId,
		UUID companyId,
		String productName,
		String companyName,
		boolean exists
) {
	public ProductInfo toApplication() {
		return new ProductInfo(productId, productName, companyName);
	}
}
