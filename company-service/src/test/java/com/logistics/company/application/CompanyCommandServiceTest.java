package com.logistics.company.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import com.logistics.template.application.service.CompanyCommandService;
import com.logistics.template.domain.entity.Company;
import com.logistics.template.domain.entity.CompanyType;
import com.logistics.template.domain.repository.CompanyCommandRepository;

@ExtendWith(MockitoExtension.class)
public class CompanyCommandServiceTest {

	UUID hubId = UUID.randomUUID();
	
	@Mock
	CompanyCommandRepository companyCommandRepository;
	
	@InjectMocks
	CompanyCommandService companyCommandService;
	
	@Nested
	@DisplayName("업체 생성")
	class CreateCompany {
		
		@Test
		@DisplayName("업체 생성 성공")
		void company_create_success() {
			String companyName = "업체 이름";
			String companyAddress = "업체 주소";
			CompanyType companyType = CompanyType.PRODUCER;
			
			Company company = Company.create(
					hubId,
					companyName,
					companyAddress,
					companyType
			);
			
			CompanyCreateCommand companyCreateCommand = new CompanyCreateCommand(
					hubId,
					companyName,
					companyAddress,
					companyType
			);
			
			given(companyCommandRepository.save(any(Company.class))).willReturn(company);
			
			Company savedCompany = companyCommandService.createCompany(companyCreateCommand);
			
			assertThat(savedCompany).isNotNull();
			assertThat(savedCompany.getCompanyName()).isEqualTo(companyName);
			assertThat(savedCompany.getCompanyType()).isEqualTo(companyType);
			assertThat(savedCompany.getCompanyAddress()).isEqualTo(companyAddress);
			
			verify(companyCommandRepository).save(any(Company.class));
		}
		
	}
	
}
