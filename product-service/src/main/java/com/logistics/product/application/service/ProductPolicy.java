package com.logistics.product.application.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.logistics.product.domain.entity.Role;

@Component
public class ProductPolicy {

	private static List<String> roles = Role.roleList();
	
	/*
	 * HUB_MANAGER
	 *  - 담당 허브 소속 업체의 상품 등록만 가능
	 *  
	 * COMPANY_MANAGER
	 *  - 본인 업체의 상품 등록만 가능
	 */
	public void createPolicyCheck(Long userId, String role) {
		
	}
	
	/*
	 * HUB_MANAGER
	 *  - 담당 허브 소속 업체의 상품 수정만 가능
	 *  
	 * COMPANY_MANAGER
	 *  - 본인 업체의 상품 수정만 가능
	 */
	public void updatePolicyCheck(Long userId, String role) {
		
	}
	
	/*
	 * HUB_MANAGER
	 *  - 해당 허브 소속의 상품만 삭제 가능
	 */
	public void deletePolicyCheck(Long userId, String role) {
		
	}
	
}
