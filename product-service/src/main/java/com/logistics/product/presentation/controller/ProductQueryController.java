package com.logistics.product.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.application.dto.result.ProductSearchResultDto;
import com.logistics.product.application.service.ProductQueryService;
import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.global.response.PageResponse;
import com.logistics.product.presentation.dto.request.ProductSearchRequestDto;
import com.logistics.product.presentation.dto.response.ProductInfoResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductQueryController {

	private final ProductQueryService productQueryService;
	
	// 상품 단건 조회
	@GetMapping("/{productId}")
	public ApiResponse<ProductInfoResponseDto> productGetOne(@PathVariable("productId") UUID productId) {
		ProductInfoResponseDto response = ProductInfoResponseDto.from(
				productQueryService.findProduct(productId)
		);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"상품 조회 성공",
				response
		);
	}
	
	// 상품 검색
	@GetMapping
	public ApiResponse<PageResponse<ProductInfoResponseDto>> productSearch(
			@Valid @ModelAttribute ProductSearchRequestDto request
	) {
		List<ProductSearchResultDto> response = productQueryService.search(request.toQuery());
		
		
		
		return null;
	}
	
}
