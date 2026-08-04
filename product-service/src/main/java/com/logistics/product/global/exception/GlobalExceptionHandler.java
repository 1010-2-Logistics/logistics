package com.logistics.product.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.logistics.product.global.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CompanyException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CompanyException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[CustomException] code = {}, message = {}", errorCode.name(), e.getMessage());
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[UnhandledException] message = {}", e.getMessage(), e);
        ErrorCode errorCode = CommonErrorCode.COMMON_INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }
}
