package com.logistics.product.application.dto.query;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.logistics.product.application.dto.command.ProductAuth;
import com.logistics.product.domain.entity.Role;
import com.logistics.product.infrastructure.security.principal.UserPrincipal;

public record ProductSearchQuery(
		UserPrincipal user,
		String productName,
		UUID companyId,
		UUID hubId,
		Pageable pageable
) implements ProductAuth {

	@Override
	public Long userId() {
		return user.getUserId();
	}

	@Override
	public Role role() {
		return user.getRole();
	}

}
