package com.logistics.product.application.facade;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.command.ProductGroupCommand.ProductCreateCommand;
import com.logistics.product.application.dto.command.ProductGroupCommand.ProductDeleteCommand;
import com.logistics.product.application.dto.command.ProductGroupCommand.ProductUpdateCommand;
import com.logistics.product.application.dto.internal.response.CompanyExistsResponseDto;
import com.logistics.product.application.dto.result.ProductCreateResultDto;
import com.logistics.product.application.dto.result.ProductUpdateResultDto;
import com.logistics.product.application.port.CompanyPort;
import com.logistics.product.application.port.HubPort;
import com.logistics.product.application.service.ProductCommandService;
import com.logistics.product.application.service.ProductPolicy;
import com.logistics.product.application.service.ProductQueryService;
import com.logistics.product.domain.entity.Product;
import com.logistics.product.global.exception.CommonErrorCode;
import com.logistics.product.global.exception.ProductErrorCode;
import com.logistics.product.global.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductCommandPacade {
	
	private final ProductCommandService productCommandService;
	
	private final ProductQueryService productQueryService;
	
	private final ProductPolicy policy;
	
	private final CompanyPort companyPort;
	
	private final HubPort hubPort;

	public ProductCreateResultDto createProduct(ProductCreateCommand command) {
		// Role 체크 - 부적절한 권한일 시 요청을 보내지 않기 위해 role 먼저 검증
		policy.validateHubManagerAndCompanyManager(command);
		
		// 업체 존재 여부 확인
		CompanyExistsResponseDto companyInfo = companyPort.companyExistsRequest(command.companyId());
		
		validateCompanySuccess(companyInfo);
		
		switch(command.role()) {
			case MASTER -> {}
			case HUB_MANAGER -> {
				// TODO: 의논후 API 스펙 확정시 구현
				// policy.canCreateHubManager();
				// 해당 업체 허브ID 의 허브매니저 user_id가 command.userId()와 같은지
				
			}
			case COMPANY_MANAGER -> policy.canProcessCompanyManager(command, companyInfo);
			
			default -> throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
		
		Product savedProduct = productCommandService.createProduct(command, companyInfo);
		
		return ProductCreateResultDto.from(savedProduct, companyInfo);
	}
	
	public ProductUpdateResultDto updateProduct(ProductUpdateCommand command) {
		// Role 체크 - 부적절한 권한일 시 요청을 보내지 않기 위해 role 먼저 검증
		policy.validateHubManagerAndCompanyManager(command);
		
		// 상품의 업체ID 를 가져오기 위해 먼저 조회
		Product product = productQueryService.findProduct(command.productId());
		
		switch(command.role()) {
			case MASTER -> {
				// role이 MASTER 인 경우 업체 매니저 조회할 필요 없음
				Product updatedProduct = productCommandService.updateProduct(command);
				
				return ProductUpdateResultDto.from(updatedProduct);
			}
			case HUB_MANAGER -> {
				// TODO: 의논후 API 스펙 확정시 구현
				// policy.canCreateHubManager();
				// 해당 업체 허브ID 의 허브매니저 user_id가 command.userId()와 같은지				
			}
			case COMPANY_MANAGER -> {
				CompanyExistsResponseDto companyInfo = companyPort.companyExistsRequest(product.getCompanyId());
				
				validateCompanySuccess(companyInfo);
				
				policy.canProcessCompanyManager(command, companyInfo);
			}
			
			default -> throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
		
		Product updatedProduct = productCommandService.updateProduct(command);
		
		return ProductUpdateResultDto.from(updatedProduct);
	}
	
	public void deleteProduct(ProductDeleteCommand command) {
		policy.validateHubManager(command);
		
		Product product = productQueryService.findProduct(command.productId());
		
		switch(command.role()) {
			case MASTER -> productCommandService.deleteProduct(product.getProductId(), command.userId());
			
			case HUB_MANAGER -> {
				// TODO: 의논후 API 스펙 확정시 구현
				// policy.canCreateHubManager();
				// 해당 업체 허브ID 의 허브매니저 user_id가 command.userId()와 같은지
				
				
				productCommandService.deleteProduct(product.getProductId(), command.userId());
			}
			
			default -> throw new ProductException(CommonErrorCode.AUTH_FORBIDDEN);
		}
		
	}
	
	private void validateCompanySuccess(CompanyExistsResponseDto companyInfo) {
		if(companyInfo.exists() == false) {
			throw new ProductException(ProductErrorCode.PRODUCT_COMPANY_NOT_FOUND);
		}
	}
	
}
