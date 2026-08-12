package com.logistics.company.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 실제 서비스로 복사할 때 SampleErrorCode -> {Domain}ErrorCode 로 이름 바꾸고,
// 네이밍 컨벤션({도메인명}_{에러타입})에 맞춰 항목을 채우세요. (예: HUB_NOT_FOUND)
@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {

  COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 업체입니다."),
  
  COMPANY_HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "허브 정보가 존재하지 않습니다."),

	COMPANY_TYPE_FOR_ORDER(HttpStatus.CONFLICT, "출발 업체는 생산 업체여야 하며, 도착 업체는 수령 업체여야 합니다."),
	
	COMPANY_USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."),
	
	COMPANY_DUPLICATE_HUB_NAME(HttpStatus.CONFLICT, "소속 허브에 동일한 업체명이 존재합니다."),
	
	COMPANY_NOT_SELF_HUB(HttpStatus.FORBIDDEN, "소속 허브가 아닙니다."),
	
	COMPANY_NOT_SELF_COMPANY(HttpStatus.FORBIDDEN, "소속 업체가 아닙니다."),

	//COMPANY_HUB_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 허브입니다."),
	
	COMPANY_NAME_UPDATE_PRODUCT_FAIL(HttpStatus.CONFLICT, "업체와 관련된 상품의 업데이트에 실패했습니다."),
	
	COMPANY_MANAGER_FIX_FAIL(HttpStatus.CONFLICT, "업체 담당자 지정에 실패하였습니다."),
	;
	
  private final HttpStatus httpStatus;
  private final String message;
}
