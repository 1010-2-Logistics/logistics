package com.logistics.template.presentation.dto.request;

import java.util.UUID;

import com.logistics.template.application.dto.command.CompanyCreateCommand;
import com.logistics.template.domain.entity.CompanyType;

import jakarta.validation.constraints.NotBlank;

public record CompanyCreateRequestDto(
		@NotBlank(message = "업체명은 필수 항목입니다.")
		String companyName,
		
		@NotBlank(message = "업체 타입은 필수 항목입니다.")
		CompanyType companyType,
		
		@NotBlank(message = "소속 허브 ID는 필수 항목입니다.")
		UUID hubId,
		
		@NotBlank(message = "업체 주소는 필수 항목입니다.")
		String companyAddress
) {
	public CompanyCreateCommand toCommand() {
		return new CompanyCreateCommand(
				hubId,
				companyName,
				companyAddress,
				companyType
		);
	}
}
