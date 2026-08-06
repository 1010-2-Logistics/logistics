package com.logistics.product.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.application.dto.result.ProductCreateResultDto;
import com.logistics.product.application.facade.ProductFacade;
import com.logistics.product.domain.entity.Role;
import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.presentation.dto.request.ProductCreateRequestDto;
import com.logistics.product.presentation.dto.request.ProductUpdateRequestDto;
import com.logistics.product.presentation.dto.response.ProductCreateResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductCommandController {

	private final ProductFacade productFacade;
	
	// 상품 생성
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ProductCreateResponseDto> createProduct(@Valid @RequestBody ProductCreateRequestDto request) {
		Long exampleUserId = 1L;
		Role exampleRole = Role.HUB_MANAGER;
		
		ProductCreateResultDto result = productFacade.createProduct(
				request.toCommand(exampleUserId, exampleRole)
		);
		
		ProductCreateResponseDto response = ProductCreateResponseDto.from(result);
		
		return ApiResponse.success(
				HttpStatus.CREATED.value(),
				"상품 등록 성공",
				response
		);
	}
	
	// 상품 수정
	@PatchMapping
	public ApiResponse<?> updateProduct(@Valid @RequestBody ProductUpdateRequestDto request) {
		
		
		return null;
	}
	
	// 상품 삭제
	@DeleteMapping("/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> deleteProduct(@PathVariable("productId") UUID productId) {
		
		return null;
	}
	
}
