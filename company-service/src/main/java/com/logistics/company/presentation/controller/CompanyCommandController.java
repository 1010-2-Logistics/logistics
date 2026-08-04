package com.logistics.company.presentation.controller;

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

import com.logistics.company.application.dto.result.CompanyCreateResultDto;
import com.logistics.company.application.dto.result.CompanyUpdateResultDto;
import com.logistics.company.application.facade.CompanyFacade;
import com.logistics.company.application.service.CompanyCommandService;
import com.logistics.company.global.response.ApiResponse;
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
	
	private final CompanyCommandService companyCommandService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<CompanyCreateResponseDto> createCompany(
			@Valid @RequestBody CompanyCreateRequestDto request) {
		CompanyCreateResultDto result = companyFacade.createCompany("인증 문자열", request.toCommand());
		
		CompanyCreateResponseDto response = CompanyCreateResponseDto.from(result);
		
		return ApiResponse.success(
				HttpStatus.CREATED.value(),
				"업체 생성 성공",
				response
		);
	}
	
	@PatchMapping("/{companyId}")
	public ApiResponse<CompanyUpdateResponseDto> updateCompany(
			@Valid @RequestBody CompanyUpdateRequestDto request,
			@PathVariable("companyId") UUID companyId) {
		CompanyUpdateResultDto result = companyCommandService.updateCompany(companyId, request.toCommand());
		
		CompanyUpdateResponseDto response = CompanyUpdateResponseDto.from(result);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"업체 수정 성공",
				response
		);
	}
	
	@DeleteMapping("/{companyId}")
	public ApiResponse<Void> deleteCompany(@PathVariable("companyId") UUID companyId) {
		Long deletedBy = 1L;
		
		companyCommandService.deleteCompany(deletedBy, companyId);
		
		return ApiResponse.success(
				HttpStatus.NO_CONTENT.value(),
				"업체 삭제 성공",
				null
		);
	}
	
}
