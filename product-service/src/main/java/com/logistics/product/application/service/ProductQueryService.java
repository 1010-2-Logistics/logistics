package com.logistics.product.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	
	public Optional<Product> findProductOptional(UUID productId) {
		return productQueryRepository.findByProductId(productId);
	}
	
	public int countCompanyId(UUID companyId) {
		return productQueryRepository.countByCompanyId(companyId);
	}
	
	public boolean existsProductName(UUID companyId, String productName) {
		return productQueryRepository.existsCompanyIdAndProductName(companyId, productName);
	}

	public Page<Product> search(List<UUID> companyIdsQuery, String productName, Pageable pageable) {
		return productQueryRepository.search(companyIdsQuery, productName, pageable);
	}
	
}
