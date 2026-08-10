package com.logistics.company.presentation.dto.request;

import java.util.UUID;

import com.logistics.company.application.dto.command.CompanyCreateCommand;
import com.logistics.company.domain.entity.CompanyType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompanyCreateRequestDto(
		@NotBlank(message = "업체명은 필수 항목입니다.")
		String companyName,
		
		Long companyManagerId,
		
		@NotNull(message = "업체 타입은 필수 항목입니다.")
		CompanyType companyType,
		
		@NotNull(message = "소속 허브 ID는 필수 항목입니다.")
		UUID hubId,
		
		@NotBlank(message = "업체 주소는 필수 항목입니다.")
		String companyAddress
) {
	public CompanyCreateCommand toCommand() {
		return new CompanyCreateCommand(
				hubId,
				companyManagerId,
				companyName,
				companyAddress,
				companyType
		);
	}
}
