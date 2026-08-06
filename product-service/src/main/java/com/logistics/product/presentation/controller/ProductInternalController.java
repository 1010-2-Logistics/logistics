package com.logistics.product.presentation.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.presentation.dto.response.OrderedProductInfoResponseDto;
import com.logistics.product.presentation.dto.response.ProductExistsResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/v1/products")
@RequiredArgsConstructor
public class ProductInternalController {

	// 주문시 상품 정보 조회
	@GetMapping("/{productId}")
	public ApiResponse<OrderedProductInfoResponseDto> orderedProductInfo(@PathVariable("productId") UUID productId) {
		
		return null;
	}
	
	// 상품 존재 여부 확인
	@GetMapping("/{productId}/exists")
	public ApiResponse<ProductExistsResponseDto> existsProduct(@PathVariable("productId") UUID productId) {
		
		return null;
	}
}
