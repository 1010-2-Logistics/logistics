package com.logistics.product.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.product.domain.entity.Product;
import com.logistics.product.domain.repository.ProductQueryRepository;
import com.logistics.product.global.exception.ProductErrorCode;
import com.logistics.product.global.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

	private final ProductQueryRepository productQueryRepository;
	
	/**
	 * deleted_at이 Null인 대상만 조회
	 * 
	 * @param productId
	 * @return
	 */
	public Product findProduct(UUID productId) {
		return productQueryRepository.findByProductId(productId)
				.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}
	
}
