package com.logistics.product.presentation.dto.response;

import java.util.UUID;

public record ProductExistsResponseDto(
		UUID productId,
		boolean exists
) {

}
