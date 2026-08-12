package com.logistics.company.presentation.controller;

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

import com.logistics.company.application.dto.result.CompanyCreateResultDto;
import com.logistics.company.application.dto.result.CompanyManagerFixResultDto;
import com.logistics.company.application.dto.result.CompanyUpdateResultDto;
import com.logistics.company.application.facade.CompanyFacade;
import com.logistics.company.application.service.CompanyCommandServiceImpl;
import com.logistics.company.global.response.ApiResponse;
import com.logistics.company.infrastructure.security.principal.UserPrincipal;
import com.logistics.company.presentation.dto.request.CompanyCreateRequestDto;
import com.logistics.company.presentation.dto.request.CompanyUpdateRequestDto;
import com.logistics.company.presentation.dto.response.CompanyCreateResponseDto;
import com.logistics.company.presentation.dto.response.CompanyUpdateResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Company")
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyCommandController {

	private final CompanyFacade companyFacade;
	
	private final CompanyCommandServiceImpl companyCommandService;
	
	@Operation(
			summary = "업체 생성",
			description = """
					접근 권한:
					  - 관리자
					  - 허브 담당자		
			"""
	)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('HUB_MANAGER', 'MASTER')")
	public ApiResponse<CompanyCreateResponseDto> createCompany(
			@AuthenticationPrincipal UserPrincipal user,
			@Valid @RequestBody CompanyCreateRequestDto request) {
		
		CompanyCreateResultDto result = companyFacade.createCompany(request.toCommand(user));
		
		CompanyCreateResponseDto response = CompanyCreateResponseDto.from(result);
		
		return ApiResponse.success(
				HttpStatus.CREATED.value(),
				"업체 생성 성공",
				response
		);
	}
	
	@Operation(
			summary = "업체 수정",
			description = """
					접근 권한:
					  - 관리자
					  - 허브 담당자: 본인 허브 소속의 업체만 수정 가능 
					  - 업체 담당자: 본인 업체만 수정 가능
			"""
	)
	@PatchMapping("/{companyId}")
	@PreAuthorize("hasRole('HUB_MANAGER', 'MASTER', 'COMPANY_MANAGER')")
	public ApiResponse<CompanyUpdateResponseDto> updateCompany(
			@AuthenticationPrincipal UserPrincipal user,
			@Valid @RequestBody CompanyUpdateRequestDto request,
			@PathVariable("companyId") UUID companyId) {
		CompanyUpdateResultDto result = companyFacade.updateCompany(companyId, request.toCommand(user));
		
		CompanyUpdateResponseDto response = CompanyUpdateResponseDto.from(result);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"업체 수정 성공",
				response
		);
	}
	
	@Operation(
			summary = "업체 담당자 지정",
			description = """
					접근 권한:
					  - 관리자
					  - 허브 담당자: 본인 허브 소속의 업체만 담당자 지정 가능
			"""
	)
	@PatchMapping("/{companyId}/{userId}")
	@PreAuthorize("hasRole('MASTER', 'HUB_MANAGER')")
	public ApiResponse<Object> companyManagerFix(
			@AuthenticationPrincipal UserPrincipal user,
			@PathVariable("companyId") UUID companyId,
			@PathVariable("userId") Long userId) {
		
		CompanyManagerFixResultDto result = companyFacade.companyManagerFix(user, companyId, userId);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"업체 담당자 지정 성공",
				result
		);
	}
	
	@Operation(
			summary = "업체 삭제",
			description = """
					접근 권한:
					  - 관리자
					  - 허브 담당자: 본인 허브 소속의 업체만 삭제 가능		
			"""
	)
	@DeleteMapping("/{companyId}")
	@PreAuthorize("hasRole('HUB_MANAGER', 'MASTER')")
	public ApiResponse<Void> deleteCompany(
			@AuthenticationPrincipal UserPrincipal user,
			@PathVariable("companyId") UUID companyId) {
		Long deletedBy = 1L;
		
		companyCommandService.deleteCompany(deletedBy, companyId, user);
		
		return ApiResponse.success(
				HttpStatus.NO_CONTENT.value(),
				"업체 삭제 성공",
				null
		);
	}
	
}
