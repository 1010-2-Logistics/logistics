package com.logistics.company.application.dto.result;

import java.util.UUID;

import com.logistics.company.domain.OrderedCompanyInfo;

public record OrderedCompanyInfoResultDto(
		UUID startCompanyId,
		UUID startHubId,
		String startCompanyAddress,
		UUID endCompanyId,
		UUID endHubId,
		String endCompanyAddress
) {
	public static OrderedCompanyInfoResultDto from(OrderedCompanyInfo companies) {
		return new OrderedCompanyInfoResultDto(
				companies.startCompanyId(),
				companies.startHubId(),
				companies.startCompanyAddress(),
				companies.endCompanyId(),
				companies.endHubId(),
				companies.endCompanyAddress()
		);
	}
}
