package com.logistics.product.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.product.application.dto.command.ProductGroupCommand.ProductDeleteCommand;
import com.logistics.product.application.dto.result.ProductCreateResultDto;
import com.logistics.product.application.dto.result.ProductUpdateResultDto;
import com.logistics.product.application.facade.ProductCommandPacade;
import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.infrastructure.security.principal.UserPrincipal;
import com.logistics.product.presentation.dto.request.ProductCreateRequestDto;
import com.logistics.product.presentation.dto.request.ProductUpdateRequestDto;
import com.logistics.product.presentation.dto.response.ProductCreateResponseDto;
import com.logistics.product.presentation.dto.response.ProductUpdateResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductCommandController {

	private final ProductCommandPacade productFacade;
	
	// 상품 생성
	@Operation(
			summary = "상품 생성",
			description = """
					접근 권한:
					  - 관리자
					  - 허브 담당자: 담당 허브 소속 업체의 상품만 생성 가능
					  - 업체 담당자: 본인 업체의 상품만 생성 가능
			"""
	)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
	public ApiResponse<ProductCreateResponseDto> createProduct(
			@AuthenticationPrincipal UserPrincipal user,
			@Valid @RequestBody ProductCreateRequestDto request) {
		ProductCreateResultDto result = productFacade.createProduct(
				request.toCommand(user.getUserId(), user.getRole())
		);
		
		ProductCreateResponseDto response = ProductCreateResponseDto.from(result);
		
		return ApiResponse.success(
				HttpStatus.CREATED.value(),
				"상품 등록 성공",
				response
		);
	}
	
	// 상품 수정
	@Operation(
			summary = "상품 수정",
			description = """
					접근 권한:
					  - 관리자
					  - 허브 담당자: 담당 허브 소속 업체의 상품만 수정 가능
					  - 업체 담당자: 본인 업체의 상품만 수정 가능
			"""
	)
	@PatchMapping
	@PreAuthorize("hasRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
	public ApiResponse<?> updateProduct(
			@AuthenticationPrincipal UserPrincipal user,
			@Valid @RequestBody ProductUpdateRequestDto request) {
		ProductUpdateResultDto result = productFacade.updateProduct(
				request.toCommand(user.getUserId(), user.getRole())
		);
		
		ProductUpdateResponseDto response = ProductUpdateResponseDto.from(result);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"상품 수정 성공",
				response
		);
	}
	
	// 상품 삭제
	@Operation(
			summary = "상품 삭제",
			description = """
					접근 권한:
					  - 관리자
					  - 허브 담당자: 본인 허브 소속 업체의 상품만 삭제 가능
			"""
	)
	@DeleteMapping("/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('MASTER', 'HUB_MANAGER')")	
	public ApiResponse<Void> deleteProduct(
			@AuthenticationPrincipal UserPrincipal user,
			@PathVariable("productId") UUID productId) {
		ProductDeleteCommand command = new ProductDeleteCommand(
				productId,
				user.getUserId(),
				user.getRole()
		);
		
		productFacade.deleteProduct(command);
		
		return ApiResponse.success(
				HttpStatus.NO_CONTENT.value(),
				"상품 삭제 성공",
				null
		);
	}
	
}
