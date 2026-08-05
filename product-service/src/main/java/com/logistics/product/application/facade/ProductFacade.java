package com.logistics.product.application.facade;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.command.ProductCommand.ProductCreateCommand;
import com.logistics.product.application.dto.command.ProductCommand.ProductDeleteCommand;
import com.logistics.product.application.dto.command.ProductCommand.ProductUpdateCommand;
import com.logistics.product.application.service.ProductCommandService;
import com.logistics.product.application.service.ProductPolicy;
import com.logistics.product.domain.entity.Product;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductFacade {
	
	private final ProductCommandService productCommandService;
	
	private final ProductPolicy policy;

	public void createProduct(ProductCreateCommand command) {
		// API 1
		policy.createPolicyCheck(command.userId(), command.role());
		
		// T 1
		Product savedProduct = productCommandService.createProduct(command);
		
		
	}
	
	public void updateProduct(ProductUpdateCommand command) {
		policy.updatePolicyCheck(command.userId(), command.role());
		
		Product savedProduct = productCommandService.updateProduct(command);
	}
	
	public void deleteProduct(ProductDeleteCommand command) {
		policy.deletePolicyCheck(command.userId(), command.role());
		
		productCommandService.deleteProduct(command);
	}
	
}
