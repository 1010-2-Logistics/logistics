package com.logistics.product.application.dto.command;

import java.util.UUID;

public class ProductCommand {

	public record ProductCreateCommand(UUID companyId, String productName, Long userId, String role) {
		
	}
	
	public record ProductUpdateCommand(UUID companyId, String productName, Long userId, String role) {
		
	}
	
}
