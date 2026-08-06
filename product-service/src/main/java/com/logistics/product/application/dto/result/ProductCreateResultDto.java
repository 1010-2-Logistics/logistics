package com.logistics.product.application.dto.result;

import java.util.UUID;

import com.logistics.product.application.dto.internal.CompanyExistsResponseDto;
import com.logistics.product.domain.entity.Product;

public record ProductCreateResultDto(
		UUID productId,
		String productName,
		UUID companyId,
		UUID hubId
) {
	public static ProductCreateResultDto from(Product product, CompanyExistsResponseDto companyInfo) {
		return new ProductCreateResultDto(
				product.getProductId(),
				product.getProductName(),
				companyInfo.companyId(),
				companyInfo.hubId()
		);
	}
	
}
