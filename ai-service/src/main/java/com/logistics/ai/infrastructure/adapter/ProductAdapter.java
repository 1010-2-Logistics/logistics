package com.logistics.ai.infrastructure.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.internal.ProductInfo;
import com.logistics.ai.application.port.out.ProductPort;
import com.logistics.ai.global.exception.AiErrorCode;
import com.logistics.ai.global.exception.AiException;
import com.logistics.ai.infrastructure.feign.client.ProductClient;
import com.logistics.ai.infrastructure.feign.response.ProductInfoResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductPort {

	private final ProductClient productClient;

	@Override
	public ProductInfo getProduct(UUID productId) {
		ProductInfoResponseDto response = productClient.getProductInfo(productId).getData();
		
		if(!response.exists()) {
			throw new AiException(AiErrorCode.AI_PRODUCT_NOT_FOUND);
		}
		
		return response.toApplication();
	}
}
