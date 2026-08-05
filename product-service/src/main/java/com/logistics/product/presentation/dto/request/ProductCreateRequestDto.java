package com.logistics.product.presentation.dto.request;

import java.util.UUID;

import com.logistics.product.application.dto.command.ProductCommand;
import com.logistics.product.application.dto.command.ProductCommand.ProductCreateCommand;

import jakarta.validation.constraints.NotNull;

public record ProductCreateRequestDto(
		@NotNull(message = "상품 생성시 업체 ID는 필수 항목입니다.")
		UUID companyId,
		
		@NotNull(message = "상품 생성시 상품명은 필수 항목입니다.")
		String productName
) {
	
	public ProductCreateCommand toCommand(Long userId, String role) {
		return new ProductCommand.ProductCreateCommand(
				companyId,
				productName,
				userId,
				role
		);
	}
	
}
