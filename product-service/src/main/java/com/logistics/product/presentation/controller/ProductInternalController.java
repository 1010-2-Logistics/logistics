package com.logistics.product.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/v1/products")
@RequiredArgsConstructor
public class ProductInternalController {

	// 주문시 상품 정보 조회
	@GetMapping("/{productId}")
	public ApiResponse<?> orderedProductInfo() {
		
		return null;
	}
	
	// 상품 존재 여부 확인
	@GetMapping("/{productId}/exists")
	public ApiResponse<?> existsProduct() {
		
		return null;
	}
}
