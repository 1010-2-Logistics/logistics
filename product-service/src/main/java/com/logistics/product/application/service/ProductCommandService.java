package com.logistics.product.application.service;

import org.springframework.stereotype.Service;

import com.logistics.product.application.dto.command.ProductCommand.ProductCreateCommand;
import com.logistics.product.application.dto.command.ProductCommand.ProductDeleteCommand;
import com.logistics.product.application.dto.command.ProductCommand.ProductUpdateCommand;
import com.logistics.product.domain.repository.ProductCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

	private final ProductCommandRepository productCommandRepository;

	private final ProductPolicy policy;
	
	public void createProduct(ProductCreateCommand command) {
		
	}
	
	public void updateProduct(ProductUpdateCommand command) {
		
	}
	
	public void deleteProduct(ProductDeleteCommand command) {
		
	}
	
}
