package com.logistics.product.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.logistics.product.domain.entity.Product;

public interface ProductQueryRepository {

	Optional<Product> findByProductId(UUID productId);

	boolean existsCompanyIdAndProductName(UUID companyId, String productName);
	
	int countByCompanyId(UUID companyId);
	
	Page<Product> search(List<UUID> companyIdsQuery, String productName, Pageable pageable);
}
