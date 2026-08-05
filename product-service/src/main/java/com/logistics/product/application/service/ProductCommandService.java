package com.logistics.product.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.product.application.dto.command.ProductCommand.ProductCreateCommand;
import com.logistics.product.application.dto.command.ProductCommand.ProductDeleteCommand;
import com.logistics.product.application.dto.command.ProductCommand.ProductUpdateCommand;
import com.logistics.product.domain.entity.Product;
import com.logistics.product.domain.repository.ProductCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

	private final ProductCommandRepository productCommandRepository;

	private final ProductQueryService productQueryService;
	
	private final ProductPolicy policy;
	
	@Transactional(rollbackFor = Exception.class)
	public Product createProduct(ProductCreateCommand command) {
		policy.createPolicyCheck(command.userId(), command.role());
		
		Product product = Product.create(
				command.companyId(),
				command.productName()
		);
		
		return productCommandRepository.save(product);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public Product updateProduct(ProductUpdateCommand command) {
		policy.updatePolicyCheck(command.userId(), command.role());
		
		Product product = productQueryService.findProduct(command.productId());
		
		product.updateProductName(command.productName());
		
		return product;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteProduct(ProductDeleteCommand command) {
		policy.deletePolicyCheck(command.userId(), command.role());
		
		Product product = productQueryService.findProduct(command.productId());
		
		product.markDeleted(command.userId());
	}
	
}
