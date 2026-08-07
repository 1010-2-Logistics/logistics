package com.logistics.product.presentation.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.application.service.ProductQueryService;
import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.presentation.dto.request.CompanyNameUpdateRequestDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/v1/products")
@RequiredArgsConstructor
public class ProductInternalCommandController {

	private final ProductQueryService productQueryService;
	
	// 업체명 수정시 업체 수정 내부 API
	@PatchMapping("/{companyId}")
	public ApiResponse<?> companyNameUpdate(@Valid @RequestBody CompanyNameUpdateRequestDto request) {
		
		return null;
	}
}
