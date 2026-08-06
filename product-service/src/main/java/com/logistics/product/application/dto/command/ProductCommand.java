package com.logistics.product.application.dto.command;

import java.util.UUID;

import com.logistics.product.domain.entity.Role;

public class ProductCommand {

	public record ProductCreateCommand(UUID companyId, String productName, Long userId, Role role) {
		
	}
	
	public record ProductUpdateCommand(UUID productId, String productName, Long userId, Role role) {
		
	}
	
	public record ProductDeleteCommand(UUID productId, Long userId, Role role) {
		
	}
	
}
