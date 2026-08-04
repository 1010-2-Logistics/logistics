package com.logistics.company.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.company.application.dto.query.CompanySearchQuery;
import com.logistics.company.application.service.CompanyQueryService;
import com.logistics.company.domain.entity.CompanyType;
import com.logistics.company.global.response.ApiResponse;
import com.logistics.company.global.response.PageResponse;
import com.logistics.company.presentation.dto.response.CompanyInfoResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyQueryController {

	private final CompanyQueryService companyQueryService;
	
	@GetMapping("/{companyId}")
	public ApiResponse<CompanyInfoResponseDto> companyGetOne(@RequestParam("companyId") UUID companyId) {
		CompanyInfoResponseDto response = CompanyInfoResponseDto.from(companyQueryService.findByCompany(companyId));
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"업체 조회 성공",
				response
		);
	}
	
	@GetMapping
	public ApiResponse<PageResponse<CompanyInfoResponseDto>> companySearch(
			@RequestParam(required = false) String companyName,
			@RequestParam(required = false) UUID hubId,
			@RequestParam(required = false) CompanyType companyType,
			Pageable pageable) {
		
		CompanySearchQuery query = new CompanySearchQuery(
				companyName, hubId, companyType, pageable
		);
		
		
		return null;
	}
	
}
