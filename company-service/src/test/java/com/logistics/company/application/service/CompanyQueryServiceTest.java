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

import com.logistics.company.application.dto.result.OrderedCompanyInfoResultDto;
import com.logistics.company.domain.OrderedCompanyInfo;
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
	
	@Nested
	@DisplayName("주문시 업체 정보 조회")
	class FindOrderedCompany {
		
		@Test
		@DisplayName("주문시 업체 정보 조회 성공")
		void companyInfo_ordered_success() {
			UUID startCompanyId = UUID.randomUUID();
			String startCompanyName = "출발 업체 이름";
			CompanyType startCompanyType = CompanyType.PRODUCER;
			UUID endCompanyId = UUID.randomUUID();
			String endCompanyName = "도착 업체 이름";
			CompanyType endCompanyType = CompanyType.RECEIVER;
			
			OrderedCompanyInfo companyInfo = new OrderedCompanyInfo(
					startCompanyId,
					startCompanyName,
					startCompanyType,
					endCompanyId,
					endCompanyName,
					endCompanyType
			);
			
			given(companyQueryRepository.findOrderedCompanyInfo(startCompanyId, endCompanyId)).willReturn(Optional.of(companyInfo));
			
			OrderedCompanyInfoResultDto result = companyQueryService.findOrderedCompanyInfo(startCompanyId, endCompanyId);
			
			assertThat(result.startCompanyId()).isEqualTo(startCompanyId);
			assertThat(result.startCompanyName()).isEqualTo(startCompanyName);
			
			assertThat(result.endCompanyId()).isEqualTo(endCompanyId);
			assertThat(result.endCompanyName()).isEqualTo(endCompanyName);
		}
		
		@Test
		@DisplayName("출발 업체는 생산 업체, 도착 업체는 수령 업체여야 한다. - CASE 1")
		void companyInfo_ordered_fail_case1() {
			UUID startCompanyId = UUID.randomUUID();
			String startCompanyName = "출발 업체 이름";
			CompanyType startCompanyType = CompanyType.RECEIVER;
			UUID endCompanyId = UUID.randomUUID();
			String endCompanyName = "도착 업체 이름";
			CompanyType endCompanyType = CompanyType.PRODUCER;
			
			assertThatThrownBy(() -> new OrderedCompanyInfo(
					startCompanyId,
					startCompanyName,
					startCompanyType,
					endCompanyId,
					endCompanyName,
					endCompanyType
			))
			.isInstanceOf(CompanyException.class)
			.hasMessage(CompanyErrorCode.COMPANY_TYPE_FOR_ORDER.getMessage());
		}
		
		@Test
		@DisplayName("출발 업체는 생산 업체, 도착 업체는 수령 업체여야 한다. - CASE 2")
		void companyInfo_ordered_fail_case2() {
			UUID startCompanyId = UUID.randomUUID();
			String startCompanyName = "출발 업체 이름";
			CompanyType startCompanyType = CompanyType.PRODUCER;
			UUID endCompanyId = UUID.randomUUID();
			String endCompanyName = "도착 업체 이름";
			CompanyType endCompanyType = CompanyType.PRODUCER;
			
			assertThatThrownBy(() -> new OrderedCompanyInfo(
					startCompanyId,
					startCompanyName,
					startCompanyType,
					endCompanyId,
					endCompanyName,
					endCompanyType
			))
			.isInstanceOf(CompanyException.class)
			.hasMessage(CompanyErrorCode.COMPANY_TYPE_FOR_ORDER.getMessage());
		}
		
		
		@Test
		@DisplayName("출발 업체는 생산 업체, 도착 업체는 수령 업체여야 한다. - CASE 3")
		void companyInfo_ordered_fail_case3() {
			UUID startCompanyId = UUID.randomUUID();
			String startCompanyName = "출발 업체 이름";
			CompanyType startCompanyType = CompanyType.RECEIVER;
			UUID endCompanyId = UUID.randomUUID();
			String endCompanyName = "도착 업체 이름";
			CompanyType endCompanyType = CompanyType.RECEIVER;
			
			assertThatThrownBy(() -> new OrderedCompanyInfo(
					startCompanyId,
					startCompanyName,
					startCompanyType,
					endCompanyId,
					endCompanyName,
					endCompanyType
					))
			.isInstanceOf(CompanyException.class)
			.hasMessage(CompanyErrorCode.COMPANY_TYPE_FOR_ORDER.getMessage());
		}
		
	}
	
}
