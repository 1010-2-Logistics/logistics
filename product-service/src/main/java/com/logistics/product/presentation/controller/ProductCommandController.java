package com.logistics.product.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductCommandController {

	// 상품 생성
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> createProduct() {
		
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
