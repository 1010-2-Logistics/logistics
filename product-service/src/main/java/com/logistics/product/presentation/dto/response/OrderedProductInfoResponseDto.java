package com.logistics.product.presentation.dto.response;

import java.util.UUID;

import com.logistics.product.domain.entity.Product;

public record OrderedProductInfoResponseDto(
		UUID productId,
    UUID companyId,
    String productName,
    String companyName,
    boolean exists
) {
	public static OrderedProductInfoResponseDto from(Product product) {
		return new OrderedProductInfoResponseDto(
				product.getProductId(),
				product.getCompanyId(),
				product.getProductName(),
				product.getCompanyName(),
				true
		);
	}
}
