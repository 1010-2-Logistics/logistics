package com.logistics.product.presentation.dto.response;

import java.util.UUID;

public record ProductInfoResponseDto(
		UUID productId,
		String productName,
		UUID companyId,
		String companyName
) {

}
