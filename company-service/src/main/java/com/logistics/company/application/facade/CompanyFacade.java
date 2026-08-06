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
		
		// 업체 담당자를 지정하여 요청을 보낸 경우
		// 업체 담당자로 지정될 companyManagerId 로 해당 회원이 존재하는지 확인
		if(command.companyManagerId() != null) {
			UserExistsResponseDto userExists = userPort.userExistsRequest(command.companyManagerId());
			
			// 해당 회원이 존재하지 않는 경우
			// 잘못된 요청이라고 판단
			if(!userExists.exists()) {
				throw new CompanyException(CompanyErrorCode.COMPANY_USER_NOT_FOUND);
			}
		}
		
		// T1 - PENDING 상태 업체 생성 완료
		// 담당자를 지정하지 않은 업체 생성 - Status:PENDING
		Company company = companyCommandService.createCompany(command);
		
		// companyManagerId 가 존재하는 경우에만 소속 변경 API 요청
		// 업체 생성 후 업체 담당자가 될 대상의 소속 업체, 소속 허브, Role 변경 요청
		if(command.companyManagerId() != null) {
			UserRoleUpdateResponseDto userRoleUpdate = userPort.companyManagerRoleUpdateRequest(
					UserRoleUpdateRequestDto.from(command.companyManagerId(), company)
			);
			
			if(userRoleUpdate.exists()) {
				// T2 - 업체 담당자가 될 대상의 소속 변경 API 가 성공한 경우
				company = companyCommandService.assignCompanyManager(company.getCompanyId(), userRoleUpdate.userId());
			} else {
				// T2 - 업체 담당자가 될 대상의 소속 변경 API 가 실패한 경우 FAILED 처리
				// 어떤 업체 담당자가 실패했는지 알아야하니 companyManagerId 저장
				company = companyCommandService.assignCompanyManagerFail(company.getCompanyId(), userRoleUpdate.userId());
			}
		}
		
		return CompanyCreateResultDto.from(hubInfo, company);
	}
	
}
