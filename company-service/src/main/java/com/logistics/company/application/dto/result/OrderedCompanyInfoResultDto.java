package com.logistics.company.application.dto.result;

import java.util.UUID;

import com.logistics.company.domain.OrderedCompanyInfo;

public record OrderedCompanyInfoResultDto(
		UUID startCompanyId,
		String startCompanyName,
		UUID endCompanyId,
		String endCompanyName
) {
	public static OrderedCompanyInfoResultDto from(OrderedCompanyInfo companies) {
		return new OrderedCompanyInfoResultDto(
				companies.startCompanyId(),
				companies.startCompanyName(),
				companies.endCompanyId(),
				companies.endCompanyName()
		);
	}
}
