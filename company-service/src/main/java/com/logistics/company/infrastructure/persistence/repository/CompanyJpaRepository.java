package com.logistics.company.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.logistics.company.domain.OrderedCompanyInfo;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyStatus;

interface CompanyJpaRepository extends JpaRepository<Company, UUID> {

	Optional<Company> findByCompanyIdAndDeletedAtIsNullAndStatus(UUID companyId, CompanyStatus status);

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
					and startCompany.status = :status
					and endCompany.status = :status
	""")
	Optional<OrderedCompanyInfo> findOrderedCompanyInfo(UUID startCompanyId, UUID endCompanyId, CompanyStatus status);

}
