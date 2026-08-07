package com.logistics.product.presentation.dto.response;

import java.util.UUID;

import com.logistics.product.application.dto.result.ProductUpdateResultDto;

public record ProductUpdateResponseDto(
		UUID productId,
		String productName
) {
	public static ProductUpdateResponseDto from(ProductUpdateResultDto result) {
		return new ProductUpdateResponseDto(
				result.productId(),
				result.productName()
		);
	}
}
