package com.logistics.company.global.exception;

import lombok.Getter;

@Getter
public class CompanyException extends RuntimeException {

    private final ErrorCode errorCode;

    public CompanyException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
