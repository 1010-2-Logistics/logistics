package com.logistics.product.application.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.internal.response.CompanyExistsResponseDto;
import com.logistics.product.application.dto.internal.response.HubAuthResponseDto;
import com.logistics.product.application.dto.query.ProductSearchQuery;
import com.logistics.product.application.port.CompanyPort;
import com.logistics.product.application.port.HubPort;
import com.logistics.product.application.service.ProductPolicy;
import com.logistics.product.application.service.ProductQueryService;
import com.logistics.product.domain.entity.Product;
import com.logistics.product.domain.entity.Role;
import com.logistics.product.global.exception.ProductErrorCode;
import com.logistics.product.global.exception.ProductException;
import com.logistics.product.infrastructure.security.principal.UserPrincipal;
import com.logistics.product.presentation.dto.response.ProductInfoResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductQueryFacade {

	private final ProductQueryService productQueryService;
	
	private final ProductPolicy policy;
	
	private final CompanyPort companyPort;
	
	private final HubPort hubPort;
	
	public Product productGetOne(UUID productId, UserPrincipal user) {
		Product product = productQueryService.findProduct(productId);
		
		CompanyExistsResponseDto companyInfo = companyPort.companyExistsRequest(productId);
		
		if(companyInfo.hubId() != user.getHubId()) {
			throw new ProductException(ProductErrorCode.PRODUCT_HUB_ACCESS_DENIED);
		}
		
		return product;
	}
	
	public Page<ProductInfoResponseDto> search(ProductSearchQuery query) {
		List<UUID> companyIdsQuery = new ArrayList<>();
		
		// Role = HUB_MANAGER + 검색 조건에 hubId 포함된 경우
		if(query.role() == Role.HUB_MANAGER && query.hubId() != null) {
			HubAuthResponseDto hubAuth = new HubAuthResponseDto(
					UUID.randomUUID(),
					1L
			);
			
			policy.validateCompanyBelongsToHub(query, query.hubId(), hubAuth);
		}
		
		if(query.hubId() != null) {
			List<UUID> companyIds = companyPort.companyIdsByHubIdRequest(query.hubId());
			
			if(companyIds.isEmpty()) {
				return Page.empty();
			}
			
			companyIdsQuery.addAll(companyIds);
		}
		
		if(query.companyId() != null) {
			if(!companyIdsQuery.isEmpty() && !companyIdsQuery.contains(query.companyId())) {
				return Page.empty();
			}
			
			companyIdsQuery = List.of(query.companyId());
		}
		
		Page<Product> productPage = productQueryService.search(
				companyIdsQuery,
				query.productName(),
				query.pageable()
		);
		
		if(productPage.isEmpty()) {
			return Page.empty();
		}
		
		return productPage.map(ProductInfoResponseDto::from);
	}
	
}
