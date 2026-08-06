package com.logistics.company.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.logistics.company.domain.OrderedCompanyInfo;
import com.logistics.company.domain.entity.Company;

interface CompanyJpaRepository extends JpaRepository<Company, UUID> {

	Optional<Company> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

	@Query("""
			select new com.logistics.company.domain.OrderedCompanyInfo(
					startCompany.companyId,
					startCompany.hubId,
					startCompany.companyAddress,
					startCompany.companyType,
					endCompany.companyId,
					endCompany.hubId,
					endCompany.companyAddress,
					endCompany.companyType
			)
			from Company startCompany, Company endCompany
			where startCompany.id = :startCompanyId
					and endCompany.id = :endCompanyId
					and startCompany.deletedAt is null
					and endCompany.deletedAt is null
	""")
	Optional<OrderedCompanyInfo> findOrderedCompanyInfo(UUID startCompanyId, UUID endCompanyId);

}
