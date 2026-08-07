package com.logistics.product.presentation.dto.response;

import java.util.UUID;

import com.logistics.product.domain.entity.Product;

public record ProductInfoResponseDto(
		UUID productId,
		String productName,
		UUID companyId,
		String companyName
) {

	public static ProductInfoResponseDto from(Product product) {
		return new ProductInfoResponseDto(
				product.getProductId(),
				product.getProductName(),
				product.getCompanyId(),
				product.getCompanyName()
		);
	}
}
