package com.logistics.product.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.application.facade.ProductQueryFacade;
import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.global.response.PageResponse;
import com.logistics.product.infrastructure.security.principal.UserPrincipal;
import com.logistics.product.presentation.dto.request.ProductSearchRequestDto;
import com.logistics.product.presentation.dto.response.ProductInfoResponseDto;
import com.logistics.product.presentation.interceptor.NoAuthentication;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductQueryController {

	private final ProductQueryFacade productQueryFacade;
	
	// 상품 단건 조회
	@GetMapping("/{productId}")
	@NoAuthentication
	public ApiResponse<ProductInfoResponseDto> productGetOne(
			@AuthenticationPrincipal UserPrincipal user,
			@PathVariable("productId") UUID productId) {
		ProductInfoResponseDto response = ProductInfoResponseDto.from(
				productQueryFacade.productGetOne(productId, user)
		);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"상품 조회 성공",
				response
		);
	}
	
	// 상품 검색
	@GetMapping
	@NoAuthentication
	public ApiResponse<PageResponse<ProductInfoResponseDto>> productSearch(
			@AuthenticationPrincipal UserPrincipal user,
			@Valid @ModelAttribute ProductSearchRequestDto request) {
		
		Page<ProductInfoResponseDto> productPage = productQueryFacade.search(request.toQuery(user));
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"상품 조회 성공",
				PageResponse.of(productPage)
		);
	}
	
}
