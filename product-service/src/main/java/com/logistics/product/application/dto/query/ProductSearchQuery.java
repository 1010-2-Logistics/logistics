package com.logistics.product.application.dto.query;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.logistics.product.application.dto.command.ProductAuth;
import com.logistics.product.domain.entity.Role;

public record ProductSearchQuery(
		Long userId,
		Role role,
		String productName,
		UUID companyId,
		UUID hubId,
		Pageable pageable
) implements ProductAuth {

}
