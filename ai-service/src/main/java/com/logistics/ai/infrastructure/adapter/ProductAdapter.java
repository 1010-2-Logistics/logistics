package com.logistics.ai.infrastructure.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.internal.ProductInfo;
import com.logistics.ai.application.port.out.ProductPort;
import com.logistics.ai.global.exception.AiErrorCode;
import com.logistics.ai.global.exception.AiException;
import com.logistics.ai.infrastructure.exception.NonRetryRemoteException;
import com.logistics.ai.infrastructure.exception.RemoteErrorCode;
import com.logistics.ai.infrastructure.exception.RetryRemoteException;
import com.logistics.ai.infrastructure.feign.client.ProductClient;
import com.logistics.ai.infrastructure.feign.response.ProductInfoResponseDto;

import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductPort {

	private final ProductClient productClient;

	@Override
	public ProductInfo getProduct(UUID productId) {
		try {
			ProductInfoResponseDto response = productClient.getProductInfo(productId).getData();
			
			return response.toApplication();
		}
		
		catch (RetryableException e) {
			log.error(
					"[AI-SERVICE]: Product Service 통신 장애. status = {}, message = {}",
					e.status(),
					e.getMessage()
			);
			throw new RetryRemoteException(
					RemoteErrorCode.PRODUCT_REMOTE_ERROR,
					e.getMessage()
			);
		}
		
		catch (FeignException.NotFound e) {
			log.warn(
					"[AI-SERVICE]: 상품 정보를 찾을 수 없습니다. productId = {}",
					productId
			);
			
			throw new NonRetryRemoteException(RemoteErrorCode.PRODUCT_INFO_NOT_FOUND);
		}
		
		catch (FeignException e) {
			if(e.status() >= 500) {
				throw new RetryRemoteException(
						RemoteErrorCode.PRODUCT_INFO_NOT_FOUND,
						e.getMessage()
				);
			}
			
			throw new NonRetryRemoteException(RemoteErrorCode.PRODUCT_REMOTE_ERROR);
		}
	}
}
