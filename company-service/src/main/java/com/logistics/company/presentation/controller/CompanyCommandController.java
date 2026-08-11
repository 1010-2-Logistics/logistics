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
import com.logistics.company.application.dto.result.CompanyUpdateResultDto;
import com.logistics.company.application.facade.CompanyFacade;
import com.logistics.company.application.service.CompanyCommandServiceImpl;
import com.logistics.company.global.response.ApiResponse;
import com.logistics.company.infrastructure.security.principal.UserPrincipal;
import com.logistics.company.presentation.dto.request.CompanyCreateRequestDto;
import com.logistics.company.presentation.dto.request.CompanyUpdateRequestDto;
import com.logistics.company.presentation.dto.response.CompanyCreateResponseDto;
import com.logistics.company.presentation.dto.response.CompanyUpdateResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyCommandController {

	private final CompanyFacade companyFacade;
	
	private final CompanyCommandServiceImpl companyCommandService;
	
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
