package com.logistics.company.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.logistics.company.application.dto.command.CompanyUpdateCommand;
import com.logistics.company.application.dto.result.CompanyUpdateResultDto;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyStatus;
import com.logistics.company.domain.entity.CompanyType;
import com.logistics.company.domain.repository.CompanyCommandRepository;
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;

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
			Long companyManagerId = 1L;
			CompanyType companyType = CompanyType.PRODUCER;
			
			Company company = Company.create(
					hubId,
					companyName,
					companyAddress,
					companyType
			);
			
			CompanyCreateCommand companyCreateCommand = new CompanyCreateCommand(
					hubId,
					companyManagerId,
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
			assertThat(savedCompany.getStatus()).isEqualTo(CompanyStatus.PENDING);
			
			// CompanyFacade 에서 command 의 companyManagerId 를 넣어줌
			assertThat(savedCompany.getCompanyManagerId()).isNull();
			
			verify(companyCommandRepository).save(any(Company.class));
		}
		
		@Test
		@DisplayName("소속 허브에 같은 업체명이 있을 경우 실패")
		void company_create_fail_1() {
			UUID hubId = UUID.randomUUID();
			Long companyManagerId = 1L;
			String companyName = "업체이름A";
			String companyAddress = "업체주소A";
			
			CompanyCreateCommand command = new CompanyCreateCommand(
					hubId, companyManagerId,
					companyName, companyAddress,
					CompanyType.PRODUCER
			);
			
			when(companyQueryService.checkCompanyNameForCreate(hubId, companyName)).thenReturn(true);
			
			assertThatThrownBy(() -> companyCommandService.createCompany(command))
			.isInstanceOf(CompanyException.class)
			.hasMessage(CompanyErrorCode.COMPANY_DUPLICATE_HUB_NAME.getMessage());
			
			verifyNoInteractions(companyCommandRepository);
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
		
		@Test
		@DisplayName("업체 수정시 동일 허브 아이디로 같은 업체명이 있으면 실패")
		void company_update_fail_1() {
			UUID hubId = UUID.randomUUID();
			UUID companyId = UUID.randomUUID();
			String companyName = "같은업체A";
			
			CompanyUpdateCommand command = new CompanyUpdateCommand(companyName);
			
			Company company = Company.create(hubId, companyName, companyName, CompanyType.PRODUCER);
			ReflectionTestUtils.setField(company, "companyId", companyId);
			when(companyQueryService.findByCompany(companyId)).thenReturn(company);
			
			when(companyQueryService.checkCompanyNameForUpdate(hubId, companyName, companyId)).thenReturn(true);
			
			assertThatThrownBy(() -> companyCommandService.updateCompany(companyId, command))
			.isInstanceOf(CompanyException.class)
			.hasMessage(CompanyErrorCode.COMPANY_DUPLICATE_HUB_NAME.getMessage());
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
