package com.logistics.product.presentation.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.global.response.PageResponse;
import com.logistics.product.presentation.dto.response.ProductInfoResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductQueryController {

	// 상품 단건 조회
	@GetMapping("/{productId}")
	public ApiResponse<ProductInfoResponseDto> productGetOne(@PathVariable("productId") UUID productId) {
		
		return null;
	}
	
	// 상품 검색
	@GetMapping
	public ApiResponse<PageResponse<ProductInfoResponseDto>> productSearch() {
		
		return null;
	}
	
}
