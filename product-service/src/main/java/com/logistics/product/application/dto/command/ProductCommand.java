package com.logistics.product.application.dto.command;

import com.logistics.product.domain.entity.Role;

public interface ProductCommand {
	Long userId();
	
	Role role();
}
