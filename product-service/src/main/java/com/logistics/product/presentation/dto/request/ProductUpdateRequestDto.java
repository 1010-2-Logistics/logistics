package com.logistics.product.presentation.dto.request;

import java.util.UUID;

import com.logistics.product.application.dto.command.ProductGroupCommand;
import com.logistics.product.application.dto.command.ProductGroupCommand.ProductUpdateCommand;
import com.logistics.product.domain.entity.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequestDto(
		@NotNull(message = "상품 ID는 필수 항목입니다.")
		UUID productId,
		
		@NotBlank(message = "상품 수정시 상품명은 필수 항목입니다.")
		@Size(min = 1, max = 20, message = "상품명은 20자 이하여야 합니다.")
		String productName
) {
	public ProductUpdateCommand toCommand(Long userId, Role role) {
		return new ProductGroupCommand.ProductUpdateCommand(
				productId,
				productName,
				userId,
				role
		);
	}
}
