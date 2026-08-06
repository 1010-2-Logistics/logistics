package com.logistics.product.application.service;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.command.ProductCommand.ProductCreateCommand;
import com.logistics.product.application.dto.internal.CompanyExistsResponseDto;
import com.logistics.product.domain.entity.CompanyType;
import com.logistics.product.domain.entity.Role;
import com.logistics.product.global.exception.CommonErrorCode;
import com.logistics.product.global.exception.ProductErrorCode;
import com.logistics.product.global.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductPolicy {

	/*
	 * HUB_MANAGER
	 *  - 담당 허브 소속 업체의 상품 등록만 가능
	 *  
	 * COMPANY_MANAGER
	 *  - 본인 업체의 상품 등록만 가능
	 *  
	 * Create 작업이 가능한 권한인 경우 true
	 */
	public boolean createPolicyRoleCheck(Long userId, Role role) {
		return isHubManagerOrCompanyManager(role);
	}
	
	public void canCreateCompanyManager(ProductCreateCommand command, CompanyExistsResponseDto company) {
		// 업체 타입이 [PRODUCER] 인지 확인
		validateProducerType(company);
		
		
		// 요청자의 Role이 COMPANY_MANAGER 인 경우
		// command에 포함된 userId(요청자 ID) 와 조회한 업체 정보의 companyManagerId 와 다른 경우
		// 본인 업체가 아니므로 Forbidden 응답
		if(!Objects.equals(command.userId(), company.companyManagerId())) {				
			throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
		
	}
	
	// TODO: 의논후 API 스펙 확정시 구현
	public void canCreateHubManager() {
		
	}
	
	/*
	 * HUB_MANAGER
	 *  - 담당 허브 소속 업체의 상품 수정만 가능
	 *  
	 * COMPANY_MANAGER
	 *  - 본인 업체의 상품 수정만 가능
	 */
	public void updatePolicyCheck(Long userId, Role role) {
		isHubManagerOrCompanyManager(role);
		
	}
	
	/*
	 * HUB_MANAGER
	 *  - 해당 허브 소속의 상품만 삭제 가능
	 */
	public void deletePolicyCheck(Long userId, Role role) {
		isHubManager(role);
		
	}
	
	private boolean isHubManagerOrCompanyManager(Role role) {
		if(role.isMaster() || role.isHubManager() || role.isCompanyManager()) {
			return true;
		}
		
		return false;
	}
	
	private boolean isHubManager(Role role) {
		if(role.isMaster() || role.isHubManager()) {
			return true;
		}
		
		return false;
	}
	
	private void validateProducerType(CompanyExistsResponseDto company) {
		if(company.companyType() != CompanyType.PRODUCER) {
			throw new ProductException(ProductErrorCode.PRODUCT_INVALID_PRODUCER_COMPANY_TYPE);
		}
	}
	
}
