package com.logistics.company.application.facade;

import org.springframework.stereotype.Component;

import com.logistics.company.application.dto.command.CompanyCreateCommand;
import com.logistics.company.application.dto.internal.request.UserRoleUpdateRequestDto;
import com.logistics.company.application.dto.internal.response.HubInfoResponseDto;
import com.logistics.company.application.dto.internal.response.UserExistsResponseDto;
import com.logistics.company.application.dto.internal.response.UserRoleUpdateResponseDto;
import com.logistics.company.application.dto.result.CompanyCreateResultDto;
import com.logistics.company.application.port.HubPort;
import com.logistics.company.application.port.UserPort;
import com.logistics.company.application.service.CompanyCommandService;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompanyFacade {

	private final CompanyCommandService companyCommandService;
	
	private final HubPort hubPort;
	
	private final UserPort userPort;
	
	// 업체 생성
	public CompanyCreateResultDto createCompany(CompanyCreateCommand command) {
		// AUTH - 인증 붙여지면 작업 시작
		
		HubInfoResponseDto hubInfo = hubPort.getHubInfo(command.hubId());
		
		// 업체 담당자로 지정될 companyManagerId 로 해당 회원이 존재하는지 확인
		UserExistsResponseDto userExists = userPort.userExistsRequest(command.companyManagerId());
		
		// 해당 회원이 존재하지 않는 경우
		// 잘못된 요청이라고 판단
		if(!userExists.exists()) {
			throw new CompanyException(CompanyErrorCode.COMPANY_USER_NOT_FOUND);
		}
		
		// T1 - PENDING 상태 업체 생성 완료
		// 회원이 존재하지 않는 경우 우선 companyManagerId 가 null인 상태
		Company company = companyCommandService.createCompany(command);
		
		// companyManagerId 가 존재하는 경우에만 소속 변경 API 요청
		// 업체 생성 후 업체 담당자가 될 대상의 소속 업체, 소속 허브, Role 변경 요청
		if(company.getCompanyManagerId() != null) {
			UserRoleUpdateResponseDto userRoleUpdate = userPort.companyManagerRoleUpdateRequest(
					UserRoleUpdateRequestDto.from(company)
			);
			
			if(userRoleUpdate.exists()) {
				// T2 - 업체 담당자가 될 대상의 소속 변경 API 가 성공한 경우
				companyCommandService.assignCompanyManager(company.getCompanyId(), userRoleUpdate.userId());
			}
		}
		
		return CompanyCreateResultDto.from(hubInfo, company);
	}
	
}
