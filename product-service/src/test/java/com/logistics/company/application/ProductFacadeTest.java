package com.logistics.company.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

import com.logistics.product.application.dto.command.ProductCommand.ProductCreateCommand;
import com.logistics.product.application.dto.internal.CompanyExistsResponseDto;
import com.logistics.product.application.dto.result.ProductCreateResultDto;
import com.logistics.product.application.facade.ProductFacade;
import com.logistics.product.application.port.CompanyPort;
import com.logistics.product.application.service.ProductCommandService;
import com.logistics.product.application.service.ProductPolicy;
import com.logistics.product.domain.entity.CompanyType;
import com.logistics.product.domain.entity.Product;
import com.logistics.product.domain.entity.Role;
import com.logistics.product.global.exception.CommonErrorCode;
import com.logistics.product.global.exception.ProductErrorCode;
import com.logistics.product.global.exception.ProductException;

@ExtendWith(MockitoExtension.class)
public class ProductFacadeTest {

	@Mock
	private ProductCommandService productCommandService;
	
	@Mock
	private ProductPolicy policy;
	
	@Mock
	private CompanyPort companyPort;
	
	@InjectMocks
	private ProductFacade productFacade;
	
	@Nested
	@DisplayName("상품 등록 테스트")
	class CreateProduct {
		
		@Test
		@DisplayName("업체 담당자는 상품 생성이 성공해야 한다.")
		void companyManager_product_create_success() {
			UUID companyId = UUID.randomUUID();
			UUID hubId = UUID.randomUUID();
			Long userId = 1L;
			String productName = "로또1등 당첨권";
			
			ProductCreateCommand command = new ProductCreateCommand(
					companyId,
					productName,
					userId,
					Role.COMPANY_MANAGER
			);
			
			CompanyExistsResponseDto companyInfo = new CompanyExistsResponseDto(
					companyId,
					CompanyType.PRODUCER,
					hubId,
					userId,	// companyManagerId
					true
			);
			
			Product savedProduct = Product.create(companyId, productName);
			
			when(policy.createPolicyRoleCheck(userId, Role.COMPANY_MANAGER)).thenReturn(true);
			
			when(companyPort.companyExistsRequest(companyId)).thenReturn(companyInfo);
			
			when(productCommandService.createProduct(command)).thenReturn(savedProduct);
			
			ProductCreateResultDto result = productFacade.createProduct(command);
			
			assertThat(result.productName()).isEqualTo(productName);
			assertThat(result.companyId()).isEqualTo(companyId);
			assertThat(result.hubId()).isEqualTo(hubId);
			
			verify(policy).createPolicyRoleCheck(userId, Role.COMPANY_MANAGER);
			verify(companyPort).companyExistsRequest(companyId);
			verify(policy).canCreateCompanyManager(command, companyInfo);
			verify(productCommandService).createProduct(command);
		}
		
		@Test
		@DisplayName("허브 관리자는 자신의 허브 소속 업체의 상품을 등록할 수 있다.")
		void hubManager_product_create_success() {
			UUID companyId = UUID.randomUUID();
			UUID hubId = UUID.randomUUID();
			Long userId = 1L;
			
			// TODO: 의논후 API 스펙 확정시 테스트 작성
		}
		
		@Test
		@DisplayName("등록 가능한 권한이 없으면 실패한다.")
		void product_create_failure_forbidden() {
			UUID companyId = UUID.randomUUID();
			Long userId = 1L;
			String productName = "제로콜라";
			
			ProductCreateCommand command = new ProductCreateCommand(
					companyId,
					productName,
					userId,
					Role.COMPANY_DELIVERY_MANAGER
			);
			
			when(policy.createPolicyRoleCheck(userId, Role.COMPANY_DELIVERY_MANAGER)).thenReturn(false);
			
			assertThatThrownBy(() -> productFacade.createProduct(command))
			.isInstanceOf(ProductException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
			
			verifyNoInteractions(companyPort);
			verifyNoInteractions(productCommandService);			
		}
		
		@Test
		@DisplayName("다른 업체의 상품을 등록하려 할 때")
		void product_create_failure_company_ga_darda() {
			UUID companyId = UUID.randomUUID();
			UUID hubId = UUID.randomUUID();
			
			Long requestUserId = 10L;
			Long responseUserId = 20L;
			
			ProductCreateCommand command = new ProductCreateCommand(
					companyId,
					"다르다 머가 업체 관리자가",
					requestUserId,	// 요청자 userId
					Role.COMPANY_MANAGER
			);
			
			CompanyExistsResponseDto companyInfo = new CompanyExistsResponseDto(
					companyId,
					CompanyType.PRODUCER,
					hubId,
					responseUserId,	// 업체쪽에서 받은 업체 매니저 userId
					true
			);
			
			when(policy.createPolicyRoleCheck(requestUserId, Role.COMPANY_MANAGER)).thenReturn(true);
			
			when(companyPort.companyExistsRequest(companyId)).thenReturn(companyInfo);
			
			doThrow(new ProductException(CommonErrorCode.AUTH_FORBIDDEN))
					.when(policy)
					.canCreateCompanyManager(command, companyInfo);
			
			assertThatThrownBy(() -> productFacade.createProduct(command))
			.isInstanceOf(ProductException.class)
			.hasMessage(CommonErrorCode.AUTH_FORBIDDEN.getMessage());
			
			verify(companyPort).companyExistsRequest(companyId);
			verify(productCommandService, never()).createProduct(any());
		}
		
		@Test
		@DisplayName("생산 업체가 아니면 상품을 등록할 수 없다.")
		void product_create_failure_not_producer_type() {
			UUID companyId = UUID.randomUUID();
			UUID hubId = UUID.randomUUID();
			String productName = "SlackId";
			Long userId = 1L;
			
			ProductCreateCommand command = new ProductCreateCommand(
					companyId,
					productName,
					userId,
					Role.COMPANY_MANAGER
			);
			
			CompanyExistsResponseDto companyInfo = new CompanyExistsResponseDto(
					companyId,
					CompanyType.RECEIVER,
					hubId,
					userId,
					true
			);
			
			when(policy.createPolicyRoleCheck(userId, Role.COMPANY_MANAGER)).thenReturn(true);
			
			when(companyPort.companyExistsRequest(companyId)).thenReturn(companyInfo);
			
			doThrow(new ProductException(ProductErrorCode.PRODUCT_INVALID_PRODUCER_COMPANY_TYPE))
					.when(policy)
					.canCreateCompanyManager(command, companyInfo);
			
			assertThatThrownBy(() -> productFacade.createProduct(command))
			.isInstanceOf(ProductException.class)
			.hasMessage(ProductErrorCode.PRODUCT_INVALID_PRODUCER_COMPANY_TYPE.getMessage());
			
			verify(productCommandService, never()).createProduct(any());
		}
		
		@Test
		@DisplayName("업체조회 했을 때 exists가 false로 조회가 안된 경우")
		void product_create_failure_exists_failse_company() {
			UUID companyId = UUID.randomUUID();
			Long userId = 1L;
			String productName = "벌써 몇시야";
			
			ProductCreateCommand command = new ProductCreateCommand(
					companyId,
					productName,
					userId,
					Role.HUB_MANAGER
			);
			
			// 조회 실패시 업체 서비스에서는
			// ErrorResponse가 아닌 ApiResponse로 데이터에 null을 넣고 exists false 준다.
			CompanyExistsResponseDto companyInfo = new CompanyExistsResponseDto(
					null, null, null, null, false
			);
			
			when(policy.createPolicyRoleCheck(userId, Role.HUB_MANAGER)).thenReturn(true);
			
			when(companyPort.companyExistsRequest(companyId)).thenReturn(companyInfo);
			
			assertThatThrownBy(() -> productFacade.createProduct(command))
			.isInstanceOf(ProductException.class)
			.hasMessage(ProductErrorCode.PRODUCT_COMPANY_NOT_FOUND.getMessage());
			
			verify(productCommandService, never()).createProduct(any());
		}
		
	}
	
}
