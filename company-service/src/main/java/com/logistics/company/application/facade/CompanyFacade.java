package com.logistics.company.application.facade;

import org.springframework.stereotype.Component;

import com.logistics.company.application.dto.command.CompanyCreateCommand;
import com.logistics.company.application.dto.internal.HubInfoResponseDto;
import com.logistics.company.application.dto.result.CompanyCreateResultDto;
import com.logistics.company.application.port.HubPort;
import com.logistics.company.application.service.CompanyCommandService;
import com.logistics.company.domain.entity.Company;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompanyFacade {

	private final CompanyCommandService companyCommandService;
	
	private final HubPort hubPort;
	
	// 업체 생성
	public CompanyCreateResultDto createCompany(Object auth, CompanyCreateCommand companyCreateCommand) {
		// AUTH - 인증 붙여지면 작업 시작
		
		// API-1
		HubInfoResponseDto hubInfo = hubPort.getHubInfo(companyCreateCommand.hubId());
		
		// T-1
		Company company = companyCommandService.createCompany(companyCreateCommand);
		
		return CompanyCreateResultDto.from(hubInfo, company);
	}
	
}
