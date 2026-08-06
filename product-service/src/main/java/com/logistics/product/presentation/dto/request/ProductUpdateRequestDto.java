package com.logistics.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequestDto(
		@NotBlank(message = "상품 수정시 상품명은 필수 항목입니다.")
		@Size(min = 1, max = 20, message = "상품명은 20자 이하여야 합니다.")
		String productName
) {

}
