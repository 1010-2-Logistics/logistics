package com.logistics.company.application.dto.command;

import java.util.UUID;

import com.logistics.company.domain.entity.Role;
import com.logistics.company.infrastructure.security.principal.UserPrincipal;

public record CompanyUpdateCommand(
		UserPrincipal user,
		String companyName
) {

	public UUID getHubIdFromAuthentication() {
		return user.getHubId();
	}
	
	public UUID getCompanyIdFromAuthentication() {
		return user.getCompanyId();
	}
	
	public Role getRoleFromAuthentication() {
		return user.getRole();
	}
}
