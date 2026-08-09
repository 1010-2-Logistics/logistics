package com.logistics.product.application.dto.result;

import java.util.UUID;

import com.logistics.product.application.dto.command.CompanyNameUpdateCommand;

public record CompanyNameUpdateResultDto(
		int productCount,
		int updateFailCount,
		UUID companyId,
		String companyName,
		boolean exists
) {
	public static CompanyNameUpdateResultDto from(int updateTargetCount, int updatedCount, CompanyNameUpdateCommand command) {
		int updateFailCount = updateTargetCount - updatedCount;
		
		boolean exists = updateFailCount == 0;
		
		return new CompanyNameUpdateResultDto(
				updatedCount,
				updateFailCount,
				command.companyId(),
				command.companyName(),
				exists
		);
	}
}