package com.logistics.company.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.company.application.dto.command.CompanyCreateCommand;
import com.logistics.company.application.dto.command.CompanyUpdateCommand;
import com.logistics.company.application.dto.result.CompanyUpdateResultDto;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyStatus;
import com.logistics.company.domain.repository.CompanyCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyCommandService {

	private final CompanyCommandRepository companyCommandRepository;
	
	private final CompanyQueryService companyQueryService;
	
	@Transactional(rollbackFor = Exception.class)
	public Company createCompany(CompanyCreateCommand companyCreateCommand) {
		// 인가 - Facade 로 할 수도
		
		Company entity = companyCreateCommand.toEntity();
		
		return companyCommandRepository.save(entity);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public Company companyManagerFix(UUID companyId, Long companyManagerId) {
		Company entity = companyQueryService.findByCompany(companyId);
		
		entity.updateCompanyManager(companyManagerId);
		entity.updateStatus(CompanyStatus.ACTIVE);
		
		return entity;
	}
	
	
	@Transactional(rollbackFor = Exception.class)
	public CompanyUpdateResultDto updateCompany(UUID companyId, CompanyUpdateCommand companyUpdateCommand) {
		// 인가 - Facade 로 할 수도
		
		Company entity = companyQueryService.findByCompany(companyId);
		
		entity.updateCompanyName(companyUpdateCommand.companyName());
		
		return CompanyUpdateResultDto.from(entity);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteCompany(Long deletedBy, UUID companyId) {
		// 인가 - Facade 로 할 수도
		
		Company entity = companyQueryService.findByCompany(companyId);
		
		entity.markDeleted(deletedBy);
	}
	
}
