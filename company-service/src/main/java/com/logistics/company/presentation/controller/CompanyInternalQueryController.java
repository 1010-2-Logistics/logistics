package com.logistics.company.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.company.application.dto.result.OrderedCompanyInfoResultDto;
import com.logistics.company.application.service.CompanyQueryService;
import com.logistics.company.global.response.ApiResponse;
import com.logistics.company.presentation.dto.response.CompanyExistsResponseDto;
import com.logistics.company.presentation.dto.response.OrderedCompanyInfoResponseDto;
import com.logistics.company.presentation.interceptor.NoAuthentication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Company Internal")
@RestController
@RequestMapping("/internal/v1/companies")
@RequiredArgsConstructor
public class CompanyInternalQueryController {
	
	private final CompanyQueryService companyQueryService;
	
	@Operation(
			summary = "주문시 업체 정보 조회"
	)
	@GetMapping
	@NoAuthentication
	public ApiResponse<OrderedCompanyInfoResponseDto> orderedCompanyInfo(
			@RequestParam("startCompanyId") UUID startCompanyId,
			@RequestParam("endCompanyId") UUID endCompanyId) {
		
		OrderedCompanyInfoResultDto result = companyQueryService.findOrderedCompanyInfo(startCompanyId, endCompanyId);
		
		OrderedCompanyInfoResponseDto response = OrderedCompanyInfoResponseDto.from(result);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"주문시 업체 정보 조회 성공",
				response
		);
	}
	
	@Operation(
			summary = "업체 존재 여부 확인"
	)
	@GetMapping("/{companyId}/exists")
	@NoAuthentication
	public ApiResponse<CompanyExistsResponseDto> existsCompany(
			@PathVariable("companyId") UUID companyId) {
		
		CompanyExistsResponseDto response = CompanyExistsResponseDto.from(companyQueryService.findOptionalByCompany(companyId));
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"업체 존재 여부 확인",
				response
		);
	}
	
	@Operation(
			summary = "허브 소속 업체 ID 리스트 조회"
	)
	@GetMapping("/ids/{hubId}")
	@NoAuthentication
	public ApiResponse<List<UUID>> getCompanyIdsByHubId(@PathVariable("hubId") UUID hubId) {
		List<UUID> response = companyQueryService.findIdsByHubId(hubId);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"소속 허브 업체 리스트 조회",
				response
		);
	}
	
}
