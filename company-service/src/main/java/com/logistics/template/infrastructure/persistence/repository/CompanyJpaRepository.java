package com.logistics.template.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistics.template.domain.entity.Company;

interface CompanyJpaRepository extends JpaRepository<Company, UUID> {

	Optional<Company> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

}
