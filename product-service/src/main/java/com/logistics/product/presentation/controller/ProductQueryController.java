package com.logistics.product.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductQueryController {

	// 상품 단건 조회
	@GetMapping("/{productId}")
	public ApiResponse<?> productGetOne() {
		
		return null;
	}
	
	// 상품 검색
	@GetMapping
	public ApiResponse<?> productSearch() {
		
		return null;
	}
	
}
