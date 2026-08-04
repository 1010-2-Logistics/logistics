package com.logistics.company.presentation.dto.request;

import com.logistics.company.application.dto.command.CompanyUpdateCommand;

import jakarta.validation.constraints.NotBlank;

public record CompanyUpdateRequestDto(
		@NotBlank(message = "수정할 업체명은 필수 항목입니다.")
		String companyName
) {
	public CompanyUpdateCommand toCommand() {
		return new CompanyUpdateCommand(companyName);
	}
}
