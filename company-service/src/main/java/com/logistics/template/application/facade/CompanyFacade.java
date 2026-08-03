package com.logistics.template.application.facade;

import org.springframework.stereotype.Component;

import com.logistics.template.application.dto.command.CompanyCreateCommand;
import com.logistics.template.application.dto.result.CompanyCreateResultDto;
import com.logistics.template.application.service.CompanyCommandService;
import com.logistics.template.domain.entity.Company;
import com.logistics.template.infrastructure.feign.client.HubClient;
import com.logistics.template.infrastructure.feign.response.HubValidationResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompanyFacade {

	private final CompanyCommandService companyCommandService;
	
	private final HubClient hubClient;
	
	// 업체 생성
	public CompanyCreateResultDto createCompany(Object auth, CompanyCreateCommand companyCreateCommand) {
		// AUTH - 인증 붙여지면 작업 시작
		
		
		// API-1
		HubValidationResponse hubInfo = hubClient.getHub(companyCreateCommand.hubId());
		
		// T-1
		Company company = companyCommandService.createCompany(companyCreateCommand);
		
		return CompanyCreateResultDto.from(hubInfo, company);
	}
	
}
