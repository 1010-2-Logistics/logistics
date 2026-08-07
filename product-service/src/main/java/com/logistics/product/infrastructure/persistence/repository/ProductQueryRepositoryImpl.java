package com.logistics.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	@Override
	public Page<Product> search(List<UUID> companyIdsQuery, String productName, Pageable pageable) {
		boolean isCompanyEmpty = (companyIdsQuery == null || companyIdsQuery.isEmpty());
		
		return jpaRepository.search(
				isCompanyEmpty ? null : companyIdsQuery,
				isCompanyEmpty,
				productName,
				pageable
		);
	}
	
}
