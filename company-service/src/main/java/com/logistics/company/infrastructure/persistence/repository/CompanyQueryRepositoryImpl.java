package com.logistics.company.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.logistics.company.domain.OrderedCompanyInfo;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyStatus;
import com.logistics.company.domain.entity.CompanyType;
import com.logistics.company.domain.repository.CompanyQueryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CompanyQueryRepositoryImpl implements CompanyQueryRepository {

  private final CompanyJpaRepository jpaRepository;

  private final CompanyQueryDslRepository dslRepository;
  
	@Override
	public Optional<Company> findByCompanyIdAndDeletedAtIsNull(UUID companyId) {
		return jpaRepository.findByCompanyIdAndDeletedAtIsNullAndStatus(companyId, CompanyStatus.ACTIVE);
	}

	@Override
	public Optional<OrderedCompanyInfo> findOrderedCompanyInfo(UUID startCompanyId, UUID endCompanyId) {
		return jpaRepository.findOrderedCompanyInfo(startCompanyId, endCompanyId, CompanyStatus.ACTIVE);
	}

	@Override
	public Page<Company> searchCompany(
			String companyName,
			UUID hubId,
			CompanyType companyType,
			Pageable pageable) {
		
		return dslRepository.searchCompany(companyName, hubId, companyType, pageable);
	}

	@Override
	public List<UUID> findIdsByHubId(UUID hubId) {
		return jpaRepository.findCompanyIdByHubIdAndDeletedAtIsNull(hubId);
	}

	@Override
	public boolean existsByHubIdAndCompanyNameAndDeletedAtIsNull(UUID hubId, String companyName) {
		return jpaRepository.existsByHubIdAndCompanyNameAndDeletedAtIsNull(hubId, companyName);
	}

	@Override
	public boolean existsByHubIdAndCompanyNameAndCompanyIdAndDeletedAtIsNull(UUID hubId, String companyName, UUID companyId) {
		return jpaRepository.existsByHubIdAndCompanyNameAndCompanyIdAndDeletedAtIsNull(hubId, companyName, companyId);
	}
  
  
}
