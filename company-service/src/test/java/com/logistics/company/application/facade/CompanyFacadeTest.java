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

import com.logistics.template.application.dto.command.CompanyCreateCommand;
import com.logistics.template.application.dto.result.CompanyCreateResultDto;
import com.logistics.template.application.facade.CompanyFacade;
import com.logistics.template.application.service.CompanyCommandService;
import com.logistics.template.domain.entity.Company;
import com.logistics.template.domain.entity.CompanyType;
import com.logistics.template.infrastructure.feign.client.HubClient;
import com.logistics.template.infrastructure.feign.response.HubValidationResponse;

@ExtendWith(MockitoExtension.class)
public class CompanyFacadeTest {

	UUID hubId = UUID.randomUUID();
	
	@InjectMocks
	CompanyFacade companyFacade;
	
	@Mock
	CompanyCommandService companyCommandService;
	
	@Mock
	HubClient hubClient;
	
	@Nested
	@DisplayName("Facade: 업체 생성 테스트")
	class CreateCompanyFacade {
		
		@Test
		@DisplayName("업체 생성 성공")
		void company_create_success() {
			Object auth = new Object();
			
			String companyName = "업체 이름";
			String companyAddress = "업체 주소";
			CompanyType companyType = CompanyType.PRODUCER;
			
			String hubName = "허브 이름";
			
			CompanyCreateCommand companyCreateCommand = new CompanyCreateCommand(
					hubId,
					companyName,
					companyAddress,
					companyType
			);
			
			HubValidationResponse hubInfo = new HubValidationResponse(
					hubId, hubName, null, null, null
			);
			given(hubClient.getHub(hubId)).willReturn(hubInfo);
			
			Company company = Company.create(hubId, companyName, companyAddress, companyType);
			given(companyCommandService.createCompany(companyCreateCommand)).willReturn(company);
			
			CompanyCreateResultDto result = companyFacade.createCompany(auth, companyCreateCommand);
			
			assertThat(result.hubId()).isEqualTo(hubId);
			assertThat(result.hubName()).isEqualTo(hubName);
			
			assertThat(result.companyName()).isEqualTo(companyName);
			assertThat(result.companyType()).isEqualTo(companyType);
			assertThat(result.companyAddress()).isEqualTo(companyAddress);
			
			verify(hubClient).getHub(hubId);
			verify(companyCommandService).createCompany(companyCreateCommand);
		}
		
	}
	
}
