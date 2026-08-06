package com.logistics.product.presentation.dto.response;

import java.util.UUID;

public record OrderedProductInfoResponseDto(
		UUID productId,
    UUID companyId,
    String productName
) {

}
