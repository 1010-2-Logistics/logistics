package com.logistics.ai.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements ErrorCode {
	
	
	;
	
  private final HttpStatus httpStatus;
  private final String message;
}
