package com.logistics.product.presentation.dto.request;

import java.util.UUID;

import com.logistics.product.application.dto.command.ProductGroupCommand;
import com.logistics.product.application.dto.command.ProductGroupCommand.ProductCreateCommand;
import com.logistics.product.domain.entity.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductCreateRequestDto(
		@NotNull(message = "상품 생성시 업체 ID는 필수 항목입니다.")
		UUID companyId,
		
		@NotBlank(message = "상품 생성시 상품명은 필수 항목입니다.")
		@Size(min = 1, max = 20, message = "상품명은 20자 이하여야 합니다.")
		String productName
) {
	
	public ProductCreateCommand toCommand(Long userId, Role role) {
		return new ProductGroupCommand.ProductCreateCommand(
				companyId,
				productName,
				userId,
				role
		);
	}
	
}
