package com.logistics.company.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.logistics.company.domain.entity.Company;

public interface CompanyCommandRepository {

  Company save(Company company);

  Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId);
}
