package com.logistics.product.presentation.dto.response;

import java.util.Optional;
import java.util.UUID;

import com.logistics.product.domain.entity.Product;

public record ProductExistsResponseDto(
		UUID productId,
		boolean exists
) {
	public static ProductExistsResponseDto from(Optional<Product> productOptional) {
		if(productOptional.isEmpty()) {
			return new ProductExistsResponseDto(null, false);
		}
		
		Product product = productOptional.get();
		
		return new ProductExistsResponseDto(
				product.getProductId(),
				true
		);
	}
}
