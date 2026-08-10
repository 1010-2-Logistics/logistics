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
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

	private final JsonMapper jsonMapper;
	
	@Override
	public Exception decode(String methodKey, Response response) {
		
		ErrorResponse errorResponse = read(response);
		
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
		} catch (IOException e) {
			log.warn("[AI-SERVICE]: FeignErrorDecoder, ErrorResponse 파싱 실패, status = {}", response.status(), e);
		}
		
		return null;
	}

	
}
