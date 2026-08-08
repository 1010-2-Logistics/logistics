package com.logistics.product.presentation.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.product.application.dto.result.CompanyNameUpdateResultDto;

public record CompanyNameUpdateResponseDto(
		int productCount,
		int updateFailCount,
		UUID companyId,
		String companyName,
		boolean exists
) {
	public static CompanyNameUpdateResponseDto from(CompanyNameUpdateResultDto result) {
		return new CompanyNameUpdateResponseDto(
				result.productCount(),
				result.updateFailCount(),
				result.companyId(),
				result.companyName(),
				result.exists()
		);
	}
	
	@JsonIgnore
	public String toMessage() {
		if(this.exists) {
			return String.format(
					"%d개의 상품의 업체명이 성공적으로 수정되었습니다.",
					productCount
			);
		}
		else {
			return String.format(
					"%d개의 상품의 업체명이 수정되었고, 소속 업체의 상품 %d개의 수정이 실패하였습니다.",
					productCount,
					updateFailCount
			);
		}
	}
}
