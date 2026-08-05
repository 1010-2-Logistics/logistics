package com.logistics.product.application.dto.command;

import java.util.UUID;

import com.logistics.product.global.exception.CommonErrorCode;
import com.logistics.product.global.exception.ProductException;

public class ProductCommand {

	public record ProductCreateCommand(UUID companyId, String productName, Long userId, String role) {
		public ProductCreateCommand {
			authNpeValidation(userId, role);
		}
		
		
	}
	
	public record ProductUpdateCommand(UUID companyId, String productName, Long userId, String role) {
		public ProductUpdateCommand {
			authNpeValidation(userId, role);
		}
		
		
	}
	
	
	private static void authNpeValidation(Long userId, String role) {
		if(userId == null) {
			throw new ProductException(CommonErrorCode.AUTH_UNAUTHORIZED);
		}
		
		if(role == null || role.isBlank()) {
			throw new ProductException(CommonErrorCode.AUTH_UNAUTHORIZED);
		}
	}
	
	
}
