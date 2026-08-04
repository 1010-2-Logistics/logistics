package com.logistics.company.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistics.company.domain.entity.Company;

interface CompanyJpaRepository extends JpaRepository<Company, UUID> {

	Optional<Company> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

}
