package com.logistics.company.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.logistics.company.global.exception.CommonErrorCode;
import com.logistics.company.global.exception.CompanyException;
import com.logistics.company.infrastructure.security.principal.UserPrincipal;

public class PrincipalTest {

	private static final UUID HUB_ID = UUID.randomUUID();
	private static final UUID COMPANY_ID = UUID.randomUUID();
	
	@Nested
	@DisplayName("MASTER 검증")
	class MasterPrincipal {
		@Test
		@DisplayName("MASTER는 hub_id 와 company_id 가 null 이면 검증에 성공함")
		void master_success() {
			UserPrincipal principal = UserPrincipal.from("1", "MASTER", null, null);
			
			assertThat(principal).isNotNull();
			
			assertThatCode(principal::validateRoleConstraints)
			.doesNotThrowAnyException();
		}
		
		@Test
		@DisplayName("MASTER는 hub_id 가 있으면 검증에 실패함")
		void master_hub_fail() {
			UserPrincipal principal = UserPrincipal.from("1", "MASTER", HUB_ID.toString(), null);
			
			assertThatThrownBy(principal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
		}
		
		@Test
		@DisplayName("MASTER는 company_id 가 있으면 검증에 실패함")
		void master_company_fail() {
			UserPrincipal principal = UserPrincipal.from("1", "MASTER", null, COMPANY_ID.toString());
			
			assertThatThrownBy(principal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
		}
		
		@Test
		@DisplayName("MASTER는 hub_id 와 company_id 가 있으면 검증에 실패함")
		void master_hub_company_fail() {
			UserPrincipal principal = UserPrincipal.from("1", "MASTER", HUB_ID.toString(), COMPANY_ID.toString());
			
			assertThatThrownBy(principal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
		}
	}
	
	@Nested
	@DisplayName("HUB_MANAGER / HUB_DELIVERY_MANAGER")
	class HubPrincipal {
		@Test
		@DisplayName("허브 담당자 / 허브 배송 담당자는 hub_id 가 있으면 검증에 성공함")
		void hub_success() {
			UserPrincipal hubPrincipal = UserPrincipal.from("1", "HUB_MANAGER", HUB_ID.toString(), null);
			
			assertThat(hubPrincipal).isNotNull();
			
			assertThatCode(hubPrincipal::validateRoleConstraints)
			.doesNotThrowAnyException();
			
			UserPrincipal hubDeliveryPrincipal = UserPrincipal.from("1", "HUB_DELIVERY_MANAGER", HUB_ID.toString(), null);
			
			assertThat(hubDeliveryPrincipal).isNotNull();
			
			assertThatCode(hubDeliveryPrincipal::validateRoleConstraints)
			.doesNotThrowAnyException();
		}
		
		@Test
		@DisplayName("허브 담당자 / 허브 배송 담당자는 hub_id 가 있고, company_id 가 있으면 검증에 실패함")
		void hub_hub_company_fail() {
			UserPrincipal hubPrincipal = UserPrincipal.from("1", "HUB_MANAGER", HUB_ID.toString(), COMPANY_ID.toString());
			
			assertThatThrownBy(hubPrincipal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
			
			UserPrincipal hubDeliveryPrincipal = UserPrincipal.from("1", "HUB_DELIVERY_MANAGER", HUB_ID.toString(), COMPANY_ID.toString());
			
			assertThatThrownBy(hubDeliveryPrincipal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
		}
		
		@Test
		@DisplayName("허브 담당자 / 허브 배송 담당자는 hub_id 가 없고, company_id 가 있으면 검증에 실패함")
		void hub_company_fail() {
			UserPrincipal hubPrincipal = UserPrincipal.from("1", "HUB_MANAGER", null, COMPANY_ID.toString());
			
			assertThatThrownBy(hubPrincipal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
			
			UserPrincipal hubDeliveryPrincipal = UserPrincipal.from("1", "HUB_DELIVERY_MANAGER", null, COMPANY_ID.toString());
			
			assertThatThrownBy(hubDeliveryPrincipal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
		}
	}
	
	@Nested
	@DisplayName("COMPANY_MANAGER / COMPANY_DELIVERY_MANAGER")
	class CompanyPrincipal {
		@Test
		@DisplayName("업체 담당자 / 업체 배송 담당자는 hub_id 가 있고, company_id 가 있으면 검증에 성공함")
		void company_success() {
			UserPrincipal companyPrincipal = UserPrincipal.from("1", "COMPANY_MANAGER", HUB_ID.toString(), COMPANY_ID.toString());
			
			assertThat(companyPrincipal).isNotNull();
			
			assertThatCode(companyPrincipal::validateRoleConstraints)
			.doesNotThrowAnyException();
			
			UserPrincipal companyDeliveryPrincipal = UserPrincipal.from("1", "COMPANY_DELIVERY_MANAGER", HUB_ID.toString(), COMPANY_ID.toString());
			
			assertThat(companyDeliveryPrincipal).isNotNull();
			
			assertThatCode(companyDeliveryPrincipal::validateRoleConstraints)
			.doesNotThrowAnyException();
		}
		
		@Test
		@DisplayName("업체 담당자 / 업체 배송 담당자는 hub_id 가 없으면 검증에 실패함")
		void company_hub_fail() {
			UserPrincipal companyPrincipal = UserPrincipal.from("1", "COMPANY_MANAGER", null, COMPANY_ID.toString());
			
			assertThatThrownBy(companyPrincipal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
			
			UserPrincipal companyDeliveryPrincipal = UserPrincipal.from("1", "COMPANY_DELIVERY_MANAGER", null, COMPANY_ID.toString());
			
			assertThatThrownBy(companyDeliveryPrincipal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
		}
		
		@Test
		@DisplayName("업체 담당자 / 업체 배송 담당자는 company_id 가 없으면 검증에 실패함")
		void company_company_fail() {
			UserPrincipal companyPrincipal = UserPrincipal.from("1", "COMPANY_MANAGER", HUB_ID.toString(), null);
			
			assertThatThrownBy(companyPrincipal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
			
			UserPrincipal companyDeliveryPrincipal = UserPrincipal.from("1", "COMPANY_DELIVERY_MANAGER", HUB_ID.toString(), null);
			
			assertThatThrownBy(companyDeliveryPrincipal::validateRoleConstraints)
			.isInstanceOf(CompanyException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
		}
		
	}
	
}
