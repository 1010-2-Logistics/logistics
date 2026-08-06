package com.logistics.product.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

import com.logistics.product.domain.entity.Product;
import com.logistics.product.domain.repository.ProductCommandRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductCommandRepositoryImpl implements ProductCommandRepository {
	
	private final ProductJpaRepository jpaRepository;
	
	@Override
	public Product save(Product product) {
		return jpaRepository.save(product);
	}

}
