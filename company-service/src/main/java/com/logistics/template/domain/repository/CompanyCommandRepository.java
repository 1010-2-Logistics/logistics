package com.logistics.template.domain.repository;

import com.logistics.template.domain.entity.Company;
import java.util.Optional;
import java.util.UUID;

public interface CompanyCommandRepository {

    Company save(Company company);

    Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId);
}
