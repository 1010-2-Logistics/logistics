package com.logistics.company.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.logistics.company.domain.OrderedCompanyInfo;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;

public interface CompanyQueryRepository {

	boolean existsByHubIdAndCompanyNameAndDeletedAtIsNull(UUID hubId, String companyName);
	
	Optional<Company> findByCompanyIdAndDeletedAtIsNull(UUID companyId);
	
	Optional<OrderedCompanyInfo> findOrderedCompanyInfo(UUID startCompanyId, UUID endCompanyId);

	Page<Company> searchCompany(
			String companyName,
			UUID hubId,
			CompanyType companyType,
			Pageable pageable
	);

	List<UUID> findIdsByHubId(UUID hubId);
}
