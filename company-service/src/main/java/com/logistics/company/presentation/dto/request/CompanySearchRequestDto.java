package com.logistics.company.presentation.dto.request;

import java.util.UUID;

import com.logistics.company.domain.entity.CompanyType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

public record CompanySearchRequestDto(
		String companyName,
		UUID hubId,
		CompanyType companyType,
		
		@Min(value = 0, message = "page는 0 이상이어야 합니다.")
		Integer page,
		
		Integer size,
		
		String sort
) {

	public CompanySearchRequestDto {
		page = page == null ? 0 : page;
		size = size == null ? 10 : size;
		sort = sort == null || sort.isBlank()
				? "created_at"
				: sort;
	}
	
	@AssertTrue(message = "companyName, hubId 중 하나는 반드시 입력해야 합니다.")
	public boolean isSearchConditionValid() {
		return hasCompanyName(companyName) || hubId != null;
	}
	
	private boolean hasCompanyName(String companyName) {
		return companyName != null && !companyName.isBlank();
	}
}
