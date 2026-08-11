package com.logistics.ai.application.dto.internal;

import java.util.UUID;

public record ProductInfo(
		UUID productId,
		String productName,
		String companyName
) {

}
