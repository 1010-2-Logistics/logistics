package com.logistics.template.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.template.application.dto.command.CompanyCreateCommand;
import com.logistics.template.domain.entity.Company;
import com.logistics.template.domain.repository.CompanyCommandRepository;

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
