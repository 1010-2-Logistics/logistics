package com.logistics.product.presentation.dto.request;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.logistics.product.application.dto.query.ProductSearchQuery;
import com.logistics.product.infrastructure.security.principal.UserPrincipal;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

public record ProductSearchRequestDto(
		String productName,
		UUID companyId,
		UUID hubId,
		
		@Min(value = 0, message = "page는 0 이상이어야 합니다.")
		Integer page,
		
		Integer size,
		
		String sort
) {

	public ProductSearchQuery toQuery(UserPrincipal user) {
		return new ProductSearchQuery(
				user,
				productName,
				companyId,
				hubId,
				this.toPageable()
		);
	}
	
	private static final List<Integer> ALLOWED_SIZES = List.of(10, 30, 50);
	
	public ProductSearchRequestDto {
		page = (page == null || page < 0) ? 0 : page;
		
		size = (size != null && ALLOWED_SIZES.contains(size)) ? size : 10;
		
		sort = (sort == null || (!"asc".equalsIgnoreCase(sort) && !"desc".equalsIgnoreCase(sort)))
				? "desc"
				: sort.toLowerCase();
	}
	
	public Pageable toPageable() {
		Sort.Direction direction = "asc".equals(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
    return PageRequest.of(page, size, direction, "createdAt");
	}
	
	@AssertTrue(message = "상품명, 업체ID, 소속 허브ID 중 하나는 반드시 입력해야 합니다.")
	public boolean isSearchConfitionValid() {
		return hasProductName(productName) || companyId != null || hubId != null;
	}
	
	private boolean hasProductName(String productName) {
		return productName != null && !productName.isBlank();
	}
}
