package com.logistics.product.domain.repository;

import com.logistics.product.domain.entity.Product;

public interface ProductCommandRepository {
	Product save(Product product);
}
