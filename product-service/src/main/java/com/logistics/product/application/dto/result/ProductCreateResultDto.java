package com.logistics.product.application.dto.result;

import java.util.UUID;

import com.logistics.product.application.dto.internal.response.CompanyExistsResponseDto;
import com.logistics.product.domain.entity.Product;

public record ProductCreateResultDto(
		UUID productId,
		String productName,
		UUID companyId,
		String companyName,
		UUID hubId
) {
	public static ProductCreateResultDto from(Product product, CompanyExistsResponseDto companyInfo) {
		return new ProductCreateResultDto(
				product.getProductId(),
				product.getProductName(),
				product.getCompanyId(),
				product.getCompanyName(),
				companyInfo.hubId()
		);
	}
	
}
