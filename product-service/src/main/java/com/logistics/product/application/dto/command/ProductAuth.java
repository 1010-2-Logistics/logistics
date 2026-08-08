package com.logistics.product.application.dto.command;

import com.logistics.product.domain.entity.Role;

public interface ProductAuth {
	Long userId();
	
	Role role();
}
