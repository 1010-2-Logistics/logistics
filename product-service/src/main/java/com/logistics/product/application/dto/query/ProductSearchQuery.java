package com.logistics.product.application.dto.query;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

public record ProductSearchQuery(
		String productName,
		UUID companyId,
		UUID hubId,
		Pageable pageable
) {

}
