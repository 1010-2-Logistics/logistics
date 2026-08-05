package com.logistics.product.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistics.product.domain.entity.Product;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

	Optional<Product> findByProductIdAndDeletedAtIsNull(UUID productId);

	boolean existsByCompanyIdAndProductName(UUID companyId, String productName);
}
