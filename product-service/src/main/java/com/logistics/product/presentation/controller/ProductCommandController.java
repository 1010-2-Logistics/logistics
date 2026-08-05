package com.logistics.product.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.application.dto.command.ProductCommand.ProductCreateCommand;
import com.logistics.product.application.service.ProductCommandService;
import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.presentation.dto.request.ProductCreateRequestDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductCommandController {

	private final ProductCommandService productCommandService;
	
	// 상품 생성
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> createProduct(@Valid @RequestBody ProductCreateRequestDto request) {
		Long exampleUserId = 1L;
		String exampleRole = "HUB_MASTER";
		
		ProductCreateCommand command = request.toCommand(exampleUserId, exampleRole);
		
		productCommandService.createProduct(command);
		
		return null;
	}
	
	// 상품 수정
	@PatchMapping("/{productId}")
	public ApiResponse<?> updateProduct() {
		
		return null;
	}
	
	// 상품 삭제
	@DeleteMapping("/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> deleteProduct() {
		
		return null;
	}
	
}
