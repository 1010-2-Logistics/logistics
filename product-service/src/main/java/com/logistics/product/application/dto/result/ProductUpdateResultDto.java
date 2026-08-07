package com.logistics.product.application.dto.result;

import java.util.UUID;

import com.logistics.product.domain.entity.Product;

public record ProductUpdateResultDto(
		UUID productId,
		String productName
) {
	public static ProductUpdateResultDto from(Product product) {
		return new ProductUpdateResultDto(
				product.getProductId(),
				product.getProductName()
		);
	}
}
