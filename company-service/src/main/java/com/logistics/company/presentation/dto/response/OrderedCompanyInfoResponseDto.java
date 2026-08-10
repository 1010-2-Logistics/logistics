package com.logistics.company.presentation.dto.response;

import java.util.UUID;

import com.logistics.company.application.dto.result.OrderedCompanyInfoResultDto;

public record OrderedCompanyInfoResponseDto(
		UUID startCompanyId,
		UUID startHubId,
		String startCompanyAddress,
		UUID endCompanyId,
		UUID endHubId,
		String endCompanyAddress
) {

	public static OrderedCompanyInfoResponseDto from(OrderedCompanyInfoResultDto result) {
		return new OrderedCompanyInfoResponseDto(
				result.startCompanyId(),
				result.startHubId(),
				result.startCompanyAddress(),
				result.endCompanyId(),
				result.endHubId(),
				result.endCompanyAddress()
		);
	}

}
