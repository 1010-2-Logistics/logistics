package com.logistics.company.application.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.company.application.dto.command.CompanyCreateCommand;
import com.logistics.company.application.dto.command.CompanyUpdateCommand;
import com.logistics.company.application.port.CompanyCommandService;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyStatus;
import com.logistics.company.domain.entity.Role;
import com.logistics.company.domain.repository.CompanyCommandRepository;
import com.logistics.company.global.exception.CommonErrorCode;
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;
import com.logistics.company.infrastructure.security.principal.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyCommandServiceImpl implements CompanyCommandService {

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
	public Company updateCompany(UUID companyId, CompanyUpdateCommand command) {
		Company entity = companyQueryService.findByCompany(companyId);
		
		if(command.getRoleFromAuthentication() == Role.HUB_MANAGER 
			 && !Objects.equals(command.getHubIdFromAuthentication(), entity.getHubId())) {
			throw new CompanyException(CompanyErrorCode.COMPANY_NOT_SELF_HUB);
		}
		
		if(command.getRoleFromAuthentication() == Role.COMPANY_MANAGER
			 && !Objects.equals(command.getCompanyIdFromAuthentication(), entity.getCompanyId())) {
			throw new CompanyException(CompanyErrorCode.COMPANY_NOT_SELF_COMPANY);
		}
		
		boolean exists = companyQueryService.checkCompanyNameForUpdate(entity.getHubId(), command.companyName(), entity.getCompanyId());
		
		if(exists) {
			throw new CompanyException(CompanyErrorCode.COMPANY_DUPLICATE_HUB_NAME);
		}
		
		entity.updateCompanyName(command.companyName());
		
		return entity;
	}
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Company updateFailCompany(UUID companyId, String companyName) {
		Company entity = companyQueryService.findByCompany(companyId);
		
		entity.updateCompanyName(companyName);
		
		return entity;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteCompany(Long deletedBy, UUID companyId, UserPrincipal user) {
		if(user.getRole() != Role.MASTER && user.getRole() != Role.HUB_MANAGER) {
			throw new CompanyException(CommonErrorCode.AUTH_FORBIDDEN);
		}
		
		Company entity = companyQueryService.findByCompany(companyId);
		
		if(!Objects.equals(user.getHubId(), entity.getHubId())) {
			throw new CompanyException(CompanyErrorCode.COMPANY_NOT_SELF_HUB);
		}
		
		entity.markDeleted(deletedBy);
	}
	
}
