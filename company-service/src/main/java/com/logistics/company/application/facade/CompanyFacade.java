package com.logistics.company.application.facade;

import java.util.UUID;

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
		// HubInfoResponseDto hubInfo = hubPort.getHubInfo(companyCreateCommand.hubId());
		
		// Postman 테스트를 위해 임시 코드 작성
		HubInfoResponseDto hubInfo = new HubInfoResponseDto(
				UUID.fromString("b83c1d92-7f2e-4e2a-918b-5a0d3f218c94"),
				"허브브브브브"
		);
		
		// T-1
		Company company = companyCommandService.createCompany(companyCreateCommand);
		
		return CompanyCreateResultDto.from(hubInfo, company);
	}
	
}
