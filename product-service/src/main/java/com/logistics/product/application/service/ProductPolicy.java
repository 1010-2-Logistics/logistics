package com.logistics.product.application.service;

import org.springframework.stereotype.Component;

import com.logistics.product.domain.entity.Role;

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
	 */
	public void createPolicyCheck(Long userId, Role role) {
		isHubManagerOrCompanyManager(role);
		
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
	
}
