package com.logistics.company.application.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.logistics.company.domain.entity.CompanyStatus;
import com.logistics.company.domain.entity.CompanyType;

@ExtendWith(MockitoExtension.class)
public class CompanyFacadeTest {

	UUID hubId = UUID.randomUUID();
	
	@InjectMocks
	CompanyFacade companyFacade;
	
	@Mock
	CompanyCommandService companyCommandService;
	
	@Mock
	HubPort hubPort;
	
	@Mock
	UserPort userPort;
	
	@Nested
	@DisplayName("Facade: 업체 생성 테스트")
	class CreateCompanyFacade {
		
		@Test
		@DisplayName("업체 생성 - 업체 담당자 지정하여 요청을 보내고 요청자가 존재함을 확인")
		void company_create_success() {
			String companyName = "업체 이름";
			String companyAddress = "업체 주소";
			Long companyManagerId = 1L;
			CompanyType companyType = CompanyType.PRODUCER;
			
			String hubName = "허브 이름";
			
			CompanyCreateCommand companyCreateCommand = new CompanyCreateCommand(
					hubId,
					companyManagerId,
					companyName,
					companyAddress,
					companyType
			);
			
			HubInfoResponseDto hubInfo = new HubInfoResponseDto(hubId, hubName);
			given(hubPort.getHubInfo(hubId)).willReturn(hubInfo);
			
			UserExistsResponseDto userExists = new UserExistsResponseDto(companyManagerId, true);
			given(userPort.userExistsRequest(companyManagerId)).willReturn(userExists);
			
			UUID companyId = UUID.randomUUID();
			Company company = Company.create(hubId, companyName, companyAddress, companyType);
			ReflectionTestUtils.setField(company, "companyId", companyId);
			given(companyCommandService.createCompany(companyCreateCommand)).willReturn(company);
			
			UserRoleUpdateResponseDto userRoleUpdate = new UserRoleUpdateResponseDto(
					company.getCompanyId(),
					company.getHubId(),
					companyCreateCommand.companyManagerId(),
					true
			);
			given(userPort.companyManagerRoleUpdateRequest(UserRoleUpdateRequestDto.from(
					companyCreateCommand.companyManagerId(),
					company
			))).willReturn(userRoleUpdate);
			
			company.updateCompanyManager(companyManagerId);
			company.updateStatus(CompanyStatus.ACTIVE);
			
			given(companyCommandService.assignCompanyManager(companyId, companyManagerId)).willReturn(company);
			
			CompanyCreateResultDto result = companyFacade.createCompany(companyCreateCommand);
			
			assertThat(result.hubId()).isEqualTo(hubId);
			assertThat(result.hubName()).isEqualTo(hubName);
			assertThat(result.status()).isEqualTo(CompanyStatus.ACTIVE);
			
			assertThat(result.companyName()).isEqualTo(companyName);
			assertThat(result.companyType()).isEqualTo(companyType);
			assertThat(result.companyAddress()).isEqualTo(companyAddress);
			
			verify(hubPort).getHubInfo(hubId);
			verify(companyCommandService).createCompany(companyCreateCommand);
			verify(companyCommandService).assignCompanyManager(company.getCompanyId(), userRoleUpdate.userId());
		}
		
	}
	
}
