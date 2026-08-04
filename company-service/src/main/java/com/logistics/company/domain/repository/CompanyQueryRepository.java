package com.logistics.company.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.logistics.company.domain.OrderedCompanyInfo;
import com.logistics.company.domain.entity.Company;

public interface CompanyQueryRepository {

	Optional<Company> findByCompanyIdAndDeletedAtIsNull(UUID companyId);
	
	Optional<OrderedCompanyInfo> findOrderedCompanyInfo(UUID startCompanyId, UUID endCompanyId);
}
