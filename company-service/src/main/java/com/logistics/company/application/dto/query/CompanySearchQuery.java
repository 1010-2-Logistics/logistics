package com.logistics.company.application.dto.query;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.logistics.company.domain.entity.CompanyType;

public record CompanySearchQuery(
		String companyName,
		UUID hubId,
		CompanyType companyType,
		Pageable pageable
) {
	
	private static final List<Integer> ALLOWED_SIZES = List.of(10, 30, 50);
	
	private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");
	
	public CompanySearchQuery {
		
		boolean hasName = companyName != null && !companyName.isBlank();
		boolean hasHubId = hubId != null;
		boolean hasType = companyType != null;
		
		if(!hasName && !hasHubId && !hasType) {
			throw new IllegalArgumentException("검색 조건(업체명, 허브 ID, 업체 타입)중 최소 하나는 필수입니다.");
		}
		
		if(pageable == null) {
			pageable = PageRequest.of(0, 10, DEFAULT_SORT);
		} else {
			int page = pageable.getPageNumber() < 0 ? 0 : pageable.getPageNumber();
			
			int size = pageable.getPageSize();
			
			if(!ALLOWED_SIZES.contains(size)) {
				size = 10;
			}
			
			Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : DEFAULT_SORT;
			
			pageable = PageRequest.of(page, size, sort);
		}
		
	}
	
}
