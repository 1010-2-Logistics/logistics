package com.logistics.company.infrastructure.security.principal;

import java.util.UUID;

import com.logistics.company.domain.entity.Role;
import com.logistics.company.global.exception.CommonErrorCode;
import com.logistics.company.global.exception.CompanyException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
	private final Long userId;
	
	private final Role role;
	
	private final UUID hubId;
	
	private final UUID companyId;
	
	public static UserPrincipal from(String userId, String role, String hubId, String companyId) {
		Long parseUserId = null;
		Role parseRole = null;
		UUID parseHubId = null;
		UUID parseCompanyId = null;
		
		try {
			if(userId != null && !userId.isBlank())
				parseUserId = Long.parseLong(userId);
			
			if(role != null && !role.isBlank())
				parseRole = Role.valueOf(role);
			
			if(hubId != null && !hubId.isBlank())
				parseHubId = UUID.fromString(hubId);
			
			if(companyId != null && !companyId.isBlank())
				parseCompanyId = UUID.fromString(companyId);
		} catch (IllegalArgumentException e) {
			return null;
		}
		
		if(parseUserId == null) {
			return null;
		}
		
		UserPrincipal principal = new UserPrincipal(
				parseUserId,
				parseRole,
				parseHubId,
				parseCompanyId
		);
		
		return principal;
	}
	
	public void validateRoleConstraints() {
		
		// MASTER
		// hubId 없어야함
		// companyId 없어야함.
		if(
				this.role == Role.MASTER
				&& (this.hubId != null || this.companyId != null)
		) throw new CompanyException(CommonErrorCode.AUTH_FORBIDDEN);
		
		// HUB_MANAGER or HUB_DELIVERY_MANAGER
		// hubId 있어야함
		// companyId 없어야함
		if(
				(this.role == Role.HUB_MANAGER || this.role == Role.HUB_DELIVERY_MANAGER)
				&& (this.hubId == null || this.companyId != null)
		) throw new CompanyException(CommonErrorCode.AUTH_FORBIDDEN);
		
		// COMPANY_MANAGER or COMPANY_DELIVERY_MANAGER
		// hubId 있어야함
		// companyId 있어야함
		if(
				(this.role == Role.COMPANY_MANAGER || this.role == Role.COMPANY_DELIVERY_MANAGER)
				&& (this.hubId == null || this.companyId == null)
		) throw new CompanyException(CommonErrorCode.AUTH_FORBIDDEN);
		
	}
	
	
}
