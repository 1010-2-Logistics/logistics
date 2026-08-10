package com.logistics.product.presentation.dto.request;

import java.util.UUID;

import com.logistics.product.application.dto.command.CompanyNameUpdateCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompanyNameUpdateRequestDto(
		
		@NotNull(message = "업체명 수정시 업체ID 가 필요합니다.")
		UUID companyId,
		
		@NotBlank(message = "업체명 수정시 업체명이 필요합니다.")
		String companyName
) {
	public CompanyNameUpdateCommand toCommand() {
		return new CompanyNameUpdateCommand(
				companyId,
				companyName
		);
	}
}
