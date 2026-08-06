package com.logistics.product.presentation.dto.response;

import java.util.UUID;

import com.logistics.product.application.dto.result.ProductCreateResultDto;

public record ProductCreateResponseDto(
		UUID productId,
		String productName,
		UUID companyId,
		UUID hubId
) {
	public static ProductCreateResponseDto from(ProductCreateResultDto reuslt) {
		return new ProductCreateResponseDto(
				reuslt.productId(),
				reuslt.productName(),
				reuslt.companyId(),
				reuslt.hubId()
		);
	}
}
