package com.logistics.ai.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.logistics.ai.global.response.ErrorResponse;
import com.logistics.ai.infrastructure.feign.exception.NonRetryRemoteException;
import com.logistics.ai.infrastructure.feign.exception.RemoteErrorCode;
import com.logistics.ai.infrastructure.feign.exception.RetryRemoteException;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

	private final JsonMapper jsonMapper;
	
	@Override
	public Exception decode(String methodKey, Response response) {
		
		ErrorResponse errorResponse = read(response);
		
		if(isRetryStatus(response.status())) {
			return new RetryRemoteException(
					errorResponse != null
					? errorResponse.getCode()
					: String.valueOf(response.status()),
					
					errorResponse != null
					? errorResponse.getMessage()
					: "[AI-SERVICE]: 내부 서비스 일시적 장애"
			);
		}
		
		if(errorResponse == null) {
			return new NonRetryRemoteException(
					String.valueOf(response.status()),
					"[AI-SERVICE]: 내부 서비스 요청 실패"
			);
		}
		
		RemoteErrorCode errorCode = RemoteErrorCode.from(errorResponse.getCode());
		
		if(errorCode != null && errorCode.isRetry()) {
			return new RetryRemoteException(
					errorResponse.getCode(),
					errorResponse.getMessage()
			);
		}
		
		return new NonRetryRemoteException(
				errorResponse.getCode(),
				errorResponse.getMessage()
		);
	}
	
	private ErrorResponse read(Response response) {
		if(response.body() == null) {
			return null;
		}
		
		try (InputStream is = response.body().asInputStream()){
			return jsonMapper.readValue(is, ErrorResponse.class);
		} catch (JacksonException e) {
			log.warn("[AI-SERVICE]: FeignErrorDecoder, ErrorResponse 파싱 실패, status = {}", response.status(), e);
		} catch (IOException e) {
			log.warn("[AI-SERVICE]: FeignErrorDecoder, ErrorResponse 읽기 실패, status = {}", response.status(), e);
		}
		
		return null;
	}

	private boolean isRetryStatus(int status) {
		return status == 500 || status == 501 || status == 502 || status == 503 || status == 504;
	}
	
}
