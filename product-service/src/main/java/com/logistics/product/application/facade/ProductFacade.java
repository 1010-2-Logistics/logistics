package com.logistics.product.application.facade;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.command.ProductCommand.ProductCreateCommand;
import com.logistics.product.application.dto.command.ProductCommand.ProductDeleteCommand;
import com.logistics.product.application.dto.command.ProductCommand.ProductUpdateCommand;
import com.logistics.product.application.dto.internal.CompanyExistsResponseDto;
import com.logistics.product.application.dto.result.ProductCreateResultDto;
import com.logistics.product.application.port.CompanyPort;
import com.logistics.product.application.service.ProductCommandService;
import com.logistics.product.application.service.ProductPolicy;
import com.logistics.product.domain.entity.Product;
import com.logistics.product.domain.entity.Role;
import com.logistics.product.global.exception.CommonErrorCode;
import com.logistics.product.global.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductFacade {
	
	private final ProductCommandService productCommandService;
	
	private final ProductPolicy policy;
	
	private final CompanyPort companyPort;

	public ProductCreateResultDto createProduct(ProductCreateCommand command) {
		// Role 체크 - 부적절한 권한일 시 요청을 보내지 않기 위해 role 먼저 검증
		if(!policy.createPolicyRoleCheck(command.userId(), command.role())) {
			throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
		
		// 업체 존재 여부 확인
		CompanyExistsResponseDto companyInfo = companyPort.companyExistsRequest(command.companyId());
		
		// 요청자의 Role에 따라 user-service 에 요청을 보낼지 말지 결정.
		if(command.role() == Role.HUB_MANAGER) {
			// TODO: 의논후 API 스펙 확정시 구현
			// policy.canCreateHubManager();
		} else if(command.role() == Role.COMPANY_MANAGER) {
			policy.canCreateCompanyManager(command, companyInfo);
		}
		
		Product savedProduct = productCommandService.createProduct(command);
		
		return ProductCreateResultDto.from(savedProduct, companyInfo);
	}
	
	public void updateProduct(ProductUpdateCommand command) {
		policy.updatePolicyCheck(command.userId(), command.role());
		
		Product savedProduct = productCommandService.updateProduct(command);
	}
	
	public void deleteProduct(ProductDeleteCommand command) {
		policy.deletePolicyCheck(command.userId(), command.role());
		
		productCommandService.deleteProduct(command);
	}
	
}
