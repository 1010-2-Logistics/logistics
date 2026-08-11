package com.logistics.product.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.application.service.ProductQueryService;
import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.presentation.dto.response.OrderedProductInfoResponseDto;
import com.logistics.product.presentation.dto.response.ProductExistsResponseDto;
import com.logistics.product.presentation.interceptor.NoAuthentication;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/v1/products")
@RequiredArgsConstructor
public class ProductInternalQueryController {

	private final ProductQueryService productQueryService;
	
	// 주문시 상품 정보 조회
	@GetMapping("/{productId}")
	@NoAuthentication
	public ApiResponse<OrderedProductInfoResponseDto> orderedProductInfo(@PathVariable("productId") UUID productId) {
		OrderedProductInfoResponseDto response = OrderedProductInfoResponseDto.from(
				productQueryService.findProduct(productId)
		);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"주문 상품 조회 성공",
				response
		);
	}
	
	// 상품 존재 여부 확인
	@GetMapping("/{productId}/exists")
	@NoAuthentication
	public ApiResponse<ProductExistsResponseDto> existsProduct(@PathVariable("productId") UUID productId) {
		ProductExistsResponseDto response = ProductExistsResponseDto.from(
				productQueryService.findProductOptional(productId)
		);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"상품 존재 여부",
				response
		);
	}
	
}
