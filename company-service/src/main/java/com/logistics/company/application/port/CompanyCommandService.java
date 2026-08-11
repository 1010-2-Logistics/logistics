package com.logistics.company.application.port;

import java.util.UUID;

import com.logistics.company.domain.entity.Company;

public interface CompanyCommandService {

	Company updateFailCompany(UUID companyId, String companyName);
}
