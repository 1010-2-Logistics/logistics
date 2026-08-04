package com.logistics.company.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.company.application.dto.result.CompanyCreateResultDto;
import com.logistics.company.application.facade.CompanyFacade;
import com.logistics.company.global.response.ApiResponse;
import com.logistics.company.presentation.dto.request.CompanyCreateRequestDto;
import com.logistics.company.presentation.dto.response.CompanyCreateResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyCommandController {

	private final CompanyFacade companyFacade;
	
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
	
	
	
}
