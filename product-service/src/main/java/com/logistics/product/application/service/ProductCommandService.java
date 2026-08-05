package com.logistics.product.application.service;

import org.springframework.stereotype.Service;

import com.logistics.product.application.dto.command.ProductCommand.ProductCreateCommand;
import com.logistics.product.domain.repository.ProductCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

	private final ProductCommandRepository productCommandRepository;

	public void createProduct(ProductCreateCommand command) {
		// 여기서 권한 검사
		
		
	}
	
}
