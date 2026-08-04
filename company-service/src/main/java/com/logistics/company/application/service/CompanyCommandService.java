package com.logistics.company.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.company.application.dto.command.CompanyCreateCommand;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.repository.CompanyCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyCommandService {

	private final CompanyCommandRepository companyCommandRepository;
	
	@Transactional(rollbackFor = Exception.class)
	public Company createCompany(CompanyCreateCommand companyCreateCommand) {
		Company entity = companyCreateCommand.toEntity();
		
		return companyCommandRepository.save(entity);
	}
	
}
