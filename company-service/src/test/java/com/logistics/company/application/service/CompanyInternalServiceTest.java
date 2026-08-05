package com.logistics.company.application.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.logistics.company.presentation.dto.response.CompanyExistsResponseDto;

@ExtendWith(MockitoExtension.class)
public class CompanyInternalServiceTest {

UUID hubId = UUID.randomUUID();
	
	@InjectMocks
	CompanyQueryService companyQueryService;
	
	@Mock
	CompanyQueryRepository companyQueryRepository;
	
	@Nested
	@DisplayName("업체 조회")
	class InternalExistsCompany {
		
		@Test
		@DisplayName("내부 API 응답 성공")
		void company_internal_exists_success() {
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
			
			CompanyExistsResponseDto result = CompanyExistsResponseDto.from(companyQueryService.findOptionalByCompany(companyId));
			
			assertThat(result.companyType()).isEqualTo(type);
			assertThat(result.exists()).isTrue();
		}
		
	}
}
