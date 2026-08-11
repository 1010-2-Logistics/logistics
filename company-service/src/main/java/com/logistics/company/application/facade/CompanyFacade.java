package com.logistics.company.application.facade;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.company.application.dto.command.CompanyCreateCommand;
import com.logistics.company.application.dto.command.CompanyUpdateCommand;
import com.logistics.company.application.dto.internal.request.CompanyNameUpdateRequestDto;
import com.logistics.company.application.dto.internal.request.UserRoleUpdateRequestDto;
import com.logistics.company.application.dto.internal.response.CompanyNameUpdateResponseDto;
import com.logistics.company.application.dto.internal.response.HubInfoResponseDto;
import com.logistics.company.application.dto.internal.response.UserExistsResponseDto;
import com.logistics.company.application.dto.internal.response.UserRoleUpdateResponseDto;
import com.logistics.company.application.dto.result.CompanyCreateResultDto;
import com.logistics.company.application.dto.result.CompanyManagerFixResultDto;
import com.logistics.company.application.dto.result.CompanyUpdateResultDto;
import com.logistics.company.application.port.HubPort;
import com.logistics.company.application.port.ProductPort;
import com.logistics.company.application.port.UserPort;
import com.logistics.company.application.service.CompanyCommandServiceImpl;
import com.logistics.company.application.service.CompanyQueryService;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.Role;
import com.logistics.company.global.exception.CommonErrorCode;
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;
import com.logistics.company.infrastructure.security.principal.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyFacade {

	private final CompanyQueryService companyQueryService;
	
	private final CompanyCommandServiceImpl companyCommandService;
	
	private final HubPort hubPort;
	
	private final UserPort userPort;
	
	private final ProductPort productPort;
	
	// 업체 담당자 지정
	public CompanyManagerFixResultDto companyManagerFix(UserPrincipal user, UUID companyId, Long userId) {
		if(user.getRole() != Role.MASTER && user.getRole() != Role.HUB_MANAGER) {
			throw new CompanyException(CommonErrorCode.AUTH_FORBIDDEN);
		}
		
		if(user.getRole() == Role.HUB_MANAGER) {
			UUID hubId = companyQueryService.findByCompanyAllStatus(companyId).getHubId();
			if(!Objects.equals(user.getHubId(), hubId)) {
				throw new CompanyException(CompanyErrorCode.COMPANY_NOT_SELF_HUB);
			}
		}
		
		Company company = companyCommandService.assignCompanyManager(companyId, userId);
		
		boolean success = false;
		
		try {
			UserRoleUpdateResponseDto userRoleUpdate = userPort.companyManagerRoleUpdateRequest(
          UserRoleUpdateRequestDto.from(userId, company)
			);
			
			success = userRoleUpdate != null && userRoleUpdate.exists();
			
		} catch (Exception e) {
			log.error("업체 담당자 지정 연동 실패 userId = {}", userId, e);
			success = false;
		}
		
		if(!success) {
			companyCommandService.assignCompanyManagerFail(companyId, userId);
			
			throw new CompanyException(CompanyErrorCode.COMPANY_MANAGER_FIX_FAIL);
		}
		
		return new CompanyManagerFixResultDto(
				success,
				companyId,
				userId
		);
	}
	
	// 업체 수정
	public CompanyUpdateResultDto updateCompany(UUID companyId, CompanyUpdateCommand command) {
		// 이전 기록 저장
		String beforeName = companyQueryService.findByCompany(companyId).getCompanyName();
		
		// companyCommandService.updateCompany 내부에서 권한검사 같이
		Company updated = companyCommandService.updateCompany(companyId, command);
		
		// 상품 쪽 업체명 변경 호출
		CompanyNameUpdateResponseDto companyNameUpdate = productPort.companyNameUpdate(
				CompanyNameUpdateRequestDto.from(companyId, command.companyName()), beforeName
		);
		
		if(!companyNameUpdate.exists()) {
			log.error(
					"[Company Service]: 상품 서비스 업데이트 실패로 인한 보상 트랜잭션 실행(원래 이름으로 복구). originalName = {}, companyId = {}, updateCount = {}, updateFailtCount = {}",
					beforeName,
					companyId,
					companyNameUpdate.updateCount(),
					companyNameUpdate.updateFailCount()
			);
			
			updated = companyCommandService.updateFailCompany(companyId, beforeName);
			
			if(!updated.getCompanyName().equals(beforeName)) {
				log.error(
						"[Company Service]: 상품 서비스 업데이트 실패로 인한 보상 트랜잭션 최종 실패, companyId = {}, beforeName = {}",
						companyId,
						beforeName
				);
			}
			
			throw new CompanyException(CompanyErrorCode.COMPANY_NAME_UPDATE_PRODUCT_FAIL);
		}
		
		return CompanyUpdateResultDto.from(
				updated,
				companyNameUpdate
		);
	}
	
	// 업체 생성
	public CompanyCreateResultDto createCompany(CompanyCreateCommand command) {
		// AUTH - 인증 붙여지면 작업 시작
		if(command.role() != Role.MASTER && command.role() != Role.HUB_MANAGER) {
			throw new CompanyException(CommonErrorCode.AUTH_FORBIDDEN);
		}
		
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
				company = companyCommandService.assignCompanyManagerFail(company.getCompanyId(), command.companyManagerId());
			}
		}
		
		return CompanyCreateResultDto.from(hubInfo, company);
	}
	
}
