package com.logistics.product.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.logistics.product.domain.entity.Product;
import com.logistics.product.domain.repository.ProductQueryRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {

	private final ProductJpaRepository jpaRepository;

	@Override
	public Optional<Product> findByProductId(UUID productId) {
		return jpaRepository.findByProductIdAndDeletedAtIsNull(productId);
	}

	@Override
	public boolean existsCompanyIdAndProductName(UUID companyId, String productName) {
		return jpaRepository.existsByCompanyIdAndProductNameAndDeletedAtIsNull(companyId, productName);
	}

	@Override
	public int countByCompanyId(UUID companyId) {
		return jpaRepository.countByCompanyIdAndDeletedAtIsNull(companyId);
	}
	
}
