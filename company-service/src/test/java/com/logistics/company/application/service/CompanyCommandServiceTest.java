package com.logistics.company.application.service;

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

import com.logistics.company.application.dto.command.CompanyCreateCommand;
import com.logistics.company.application.dto.command.CompanyUpdateCommand;
import com.logistics.company.application.dto.result.CompanyUpdateResultDto;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;
import com.logistics.company.domain.repository.CompanyCommandRepository;

@ExtendWith(MockitoExtension.class)
public class CompanyCommandServiceTest {

	UUID hubId = UUID.randomUUID();
	
	@Mock
	CompanyCommandRepository companyCommandRepository;
	
	@Mock
	CompanyQueryService companyQueryService;
	
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
	
	@Nested
	@DisplayName("업체 수정")
	class UpdateCompany {
		
		@Test
		@DisplayName("업체 수정 성공")
		void company_update_success() {
			UUID companyId = UUID.randomUUID();
			
			String beforeCompanyName = "업체 이름 A";
			String companyAddress = "업체 주소";
			CompanyType companyType = CompanyType.PRODUCER;
			
			Company company = Company.create(
					hubId,
					beforeCompanyName,
					companyAddress,
					companyType
			);
			
			String updatedCompanyName = "업체 이름 B";
			
			CompanyUpdateCommand companyUpdateCommand = new CompanyUpdateCommand(updatedCompanyName);
			
			given(companyQueryService.findByCompany(companyId)).willReturn(company);
			
			CompanyUpdateResultDto result = companyCommandService.updateCompany(companyId, companyUpdateCommand);
			
			assertThat(result.companyName()).isEqualTo(updatedCompanyName);
			assertThat(result.companyAddress()).isEqualTo(companyAddress);
			assertThat(result.companyType()).isEqualByComparingTo(companyType);
			
			assertThat(company.getCompanyName()).isEqualTo(updatedCompanyName);
			
			verify(companyQueryService).findByCompany(companyId);
		}
		
	}
	
	@Nested
	@DisplayName("업체 삭제")
	class DeleteCompany {
		
		@Test
		@DisplayName("업체 삭제 성공")
		void company_delete_success() {
			UUID companyId = UUID.randomUUID();
			Long deletedBy = 1L;
			
			String companyName = "업체 이름 A";
			String companyAddress = "업체 주소";
			CompanyType companyType = CompanyType.PRODUCER;
			
			Company company = Company.create(
					hubId,
					companyName,
					companyAddress,
					companyType
			);
			
			given(companyQueryService.findByCompany(companyId)).willReturn(company);
			
			companyCommandService.deleteCompany(deletedBy, companyId);
			
			assertThat(company.getDeletedAt()).isNotNull();
			assertThat(company.getDeletedBy()).isEqualTo(deletedBy);
			
			verify(companyQueryService).findByCompany(companyId);
		}
		
	}
	
}
