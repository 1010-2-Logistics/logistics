package com.logistics.user.infrastructure.feign.request;

// GET 요청이면 보통 필요 없고, POST/PUT처럼 body가 필요한 Feign 호출에 사용하세요.
public record HubValidationRequest(String reason) {
}
