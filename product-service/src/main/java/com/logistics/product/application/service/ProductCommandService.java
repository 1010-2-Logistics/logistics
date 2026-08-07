package com.logistics.product.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.product.application.dto.command.ProductGroupCommand.ProductCreateCommand;
import com.logistics.product.application.dto.command.ProductGroupCommand.ProductUpdateCommand;
import com.logistics.product.domain.entity.Product;
import com.logistics.product.domain.repository.ProductCommandRepository;
import com.logistics.product.global.exception.ProductErrorCode;
import com.logistics.product.global.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

	private final ProductCommandRepository productCommandRepository;

	private final ProductQueryService productQueryService;
	
	@Transactional(rollbackFor = Exception.class)
	public Product createProduct(ProductCreateCommand command) {
		if(productQueryService.existsProductName(command.companyId(), command.productName())) {
			throw new ProductException(ProductErrorCode.PRODUCT_EXISTS_PRODUCT_NAME);
		}
		
		Product product = Product.create(
				command.companyId(),
				command.productName()
		);
		
		return productCommandRepository.save(product);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public Product updateProduct(ProductUpdateCommand command) {
		Product product = productQueryService.findProduct(command.productId());
		
		product.updateProductName(command.productName());
		
		return product;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteProduct(UUID productId, Long deletedBy) {
		Product product = productQueryService.findProduct(productId);
		
		product.markDeleted(deletedBy);
	}
	
}
