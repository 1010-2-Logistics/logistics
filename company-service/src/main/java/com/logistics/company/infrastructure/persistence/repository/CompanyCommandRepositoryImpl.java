package com.logistics.company.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.repository.CompanyCommandRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CompanyCommandRepositoryImpl implements CompanyCommandRepository {

  private final CompanyJpaRepository jpaRepository;

	@Override
	public Company save(Company company) {
		return jpaRepository.save(company);
	}

	@Override
	public Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId) {
		return jpaRepository.findByCompanyIdAndDeletedAtIsNull(companyId);
	}
  
  
}
