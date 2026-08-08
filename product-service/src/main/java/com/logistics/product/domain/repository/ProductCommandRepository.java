package com.logistics.product.domain.repository;

import java.util.UUID;

import com.logistics.product.domain.entity.Product;

public interface ProductCommandRepository {
	Product save(Product product);
	
	int companyNameUpdate(UUID companyId, String companyName);
}
