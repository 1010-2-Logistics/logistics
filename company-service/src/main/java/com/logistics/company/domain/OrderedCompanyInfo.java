package com.logistics.company.domain;

import java.util.UUID;

import com.logistics.company.domain.entity.CompanyType;
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;

public record OrderedCompanyInfo(
		UUID startCompanyId,
		String startCompanyName,
		CompanyType startCompanyType,
		UUID endCompanyId,
		String endCompanyName,
		CompanyType endCompanyType
) {

	public OrderedCompanyInfo {
		if(startCompanyType != CompanyType.PRODUCER || endCompanyType != CompanyType.RECEIVER) {
			throw new CompanyException(CompanyErrorCode.COMPANY_TYPE_FOR_ORDER);
		}
	}
}
