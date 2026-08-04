package com.logistics.company.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.repository.CompanyQueryRepository;
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyQueryService {

	private final CompanyQueryRepository companyQueryRepository;
	
	public Company findByCompany(UUID companyId) {
		return companyQueryRepository.findByCompanyIdAndDeletedAtIsNull(companyId)
				.orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_NOT_FOUND));
	}
}
