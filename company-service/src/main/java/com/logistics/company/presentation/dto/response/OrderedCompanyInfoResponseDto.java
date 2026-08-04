package com.logistics.company.presentation.dto.response;

import java.util.UUID;

import com.logistics.company.application.dto.result.OrderedCompanyInfoResultDto;

public record OrderedCompanyInfoResponseDto(
		UUID startCompanyId,
		String startCompanyName,
		UUID endCompanyId,
		String endCompanyName
) {

	public static OrderedCompanyInfoResponseDto from(OrderedCompanyInfoResultDto result) {
		return new OrderedCompanyInfoResponseDto(
				result.startCompanyId(),
				result.startCompanyName(),
				result.endCompanyId(),
				result.endCompanyName()
		);
	}

}
