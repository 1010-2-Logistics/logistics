package com.logistics.product.application.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.command.ProductAuth;
import com.logistics.product.application.dto.internal.response.CompanyExistsResponseDto;
import com.logistics.product.application.dto.internal.response.HubAuthResponseDto;
import com.logistics.product.domain.entity.CompanyType;
import com.logistics.product.global.exception.CommonErrorCode;
import com.logistics.product.global.exception.ProductErrorCode;
import com.logistics.product.global.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductPolicy {

	public void canProcessCompanyManager(ProductAuth auth, CompanyExistsResponseDto company) {
		validateProducerType(company);
		
		if(!Objects.equals(auth.userId(), company.companyManagerId())) {				
			throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
	}
	
	public void validateHubManagerAndCompanyManager(ProductAuth auth) {
		if(!auth.role().isMaster() && !auth.role().isHubManager() && !auth.role().isCompanyManager()) {
			throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
	}
	
	public void validateHubManager(ProductAuth auth) {
		if(!auth.role().isMaster() && !auth.role().isHubManager()) {
			throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
	}
	
	public void validateCompanyBelongsToHub(ProductAuth auth, UUID hubId, HubAuthResponseDto hubAuth) {
		if(!Objects.equals(auth.userId(), hubAuth.hubManagerId()) &&
				!Objects.equals(hubId, hubAuth.hubId())) {
			throw new ProductException(ProductErrorCode.PRODUCT_HUB_ACCESS_DENIED);
		}
	}
	
	private void validateProducerType(CompanyExistsResponseDto company) {
		if(company.companyType() != CompanyType.PRODUCER) {
			throw new ProductException(ProductErrorCode.PRODUCT_INVALID_PRODUCER_COMPANY_TYPE);
		}
	}
	
}
