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
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyCommandService {

	private final CompanyCommandRepository companyCommandRepository;
	
	private final CompanyQueryService companyQueryService;
	
	@Transactional(rollbackFor = Exception.class)
	public Company createCompany(CompanyCreateCommand command) {
		boolean exists = companyQueryService.checkCompanyNameForCreate(command.hubId(), command.companyName());
		
		if(exists) {
			throw new CompanyException(CompanyErrorCode.COMPANY_DUPLICATE_HUB_NAME);
		}
		
		Company entity = command.toEntity();
		
		return companyCommandRepository.save(entity);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public Company assignCompanyManager(UUID companyId, Long companyManagerId) {
		Company entity = companyQueryService.findByCompany(companyId);
		
		entity.updateCompanyManager(companyManagerId);
		entity.updateStatus(CompanyStatus.ACTIVE);
		
		return entity;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public Company assignCompanyManagerFail(UUID companyId, Long companyManagerId) {
		Company entity = companyQueryService.findByCompany(companyId);
		
		entity.updateCompanyManager(companyManagerId);
		entity.updateStatus(CompanyStatus.FAILED);
		
		return entity;
	}
	
	
	@Transactional(rollbackFor = Exception.class)
	public CompanyUpdateResultDto updateCompany(UUID companyId, CompanyUpdateCommand command) {
		Company entity = companyQueryService.findByCompany(companyId);
		
		boolean exists = companyQueryService.checkCompanyNameForUpdate(entity.getHubId(), command.companyName(), entity.getCompanyId());
		
		if(exists) {
			throw new CompanyException(CompanyErrorCode.COMPANY_DUPLICATE_HUB_NAME);
		}
		
		entity.updateCompanyName(command.companyName());
		
		return CompanyUpdateResultDto.from(entity);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteCompany(Long deletedBy, UUID companyId) {
		Company entity = companyQueryService.findByCompany(companyId);
		
		entity.markDeleted(deletedBy);
	}
	
}
