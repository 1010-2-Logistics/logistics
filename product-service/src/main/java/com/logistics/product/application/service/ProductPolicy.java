package com.logistics.product.application.service;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.command.ProductCommand;
import com.logistics.product.application.dto.internal.response.CompanyExistsResponseDto;
import com.logistics.product.domain.entity.CompanyType;
import com.logistics.product.global.exception.CommonErrorCode;
import com.logistics.product.global.exception.ProductErrorCode;
import com.logistics.product.global.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductPolicy {

	public void canProcessCompanyManager(ProductCommand command, CompanyExistsResponseDto company) {
		validateProducerType(company);
		
		if(!Objects.equals(command.userId(), company.companyManagerId())) {				
			throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
	}
	
	public void validateHubManagerAndCompanyManager(ProductCommand command) {
		if(!command.role().isMaster() || !command.role().isHubManager() || !command.role().isCompanyManager()) {
			throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
	}
	
	public void validateHubManager(ProductCommand command) {
		if(!command.role().isMaster() || !command.role().isHubManager()) {
			throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
	}
	
	private void validateProducerType(CompanyExistsResponseDto company) {
		if(company.companyType() != CompanyType.PRODUCER) {
			throw new ProductException(ProductErrorCode.PRODUCT_INVALID_PRODUCER_COMPANY_TYPE);
		}
	}
	
}
