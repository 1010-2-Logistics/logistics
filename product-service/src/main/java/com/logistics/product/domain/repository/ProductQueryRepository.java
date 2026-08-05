package com.logistics.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.logistics.product.domain.entity.Product;

public interface ProductQueryRepository {

	Optional<Product> findByProductId(UUID productId);

	boolean existsCompanyIdAndProductName(UUID companyId, String productName);
}
