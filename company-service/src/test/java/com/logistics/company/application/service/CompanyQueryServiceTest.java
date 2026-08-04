package com.logistics.company.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;
import com.logistics.company.domain.repository.CompanyQueryRepository;
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;

@ExtendWith(MockitoExtension.class)
public class CompanyQueryServiceTest {

	UUID hubId = UUID.randomUUID();
	
	@InjectMocks
	CompanyQueryService companyQueryService;
	
	@Mock
	CompanyQueryRepository companyQueryRepository;
	
	@Nested
	@DisplayName("업체 조회")
	class FindCompany {
		
		@Test
		@DisplayName("업체 조회 성공")
		void company_find_success() {
			UUID companyId = UUID.randomUUID();
			String name = "업체 이름";
			CompanyType type = CompanyType.PRODUCER;
			String address = "업체 주소";
			
			Company company = Company.create(
					hubId,
					name,
					address,
					type
			);
			
			given(companyQueryRepository.findByCompanyIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
			
			Company result = companyQueryService.findByCompany(companyId);
			
			assertThat(result.getHubId()).isEqualTo(hubId);
			assertThat(result.getCompanyName()).isEqualTo(name);
			assertThat(result.getCompanyType()).isEqualTo(type);
			assertThat(result.getCompanyAddress()).isEqualTo(address);
		}
		
		@Test
		@DisplayName("삭제된 업체인 경우 조회 실패")
		void company_find_not_found() {
			UUID companyId = UUID.randomUUID();
			
			given(companyQueryRepository.findByCompanyIdAndDeletedAtIsNull(companyId)).willReturn(Optional.empty());
			
			assertThatThrownBy(() -> companyQueryService.findByCompany(companyId))
			.isInstanceOf(CompanyException.class)
			.hasMessage(CompanyErrorCode.COMPANY_NOT_FOUND.getMessage());
		}
		
	}
	
}
