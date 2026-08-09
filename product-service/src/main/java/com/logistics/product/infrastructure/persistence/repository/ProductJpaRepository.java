package com.logistics.product.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.logistics.product.domain.entity.Product;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

	Optional<Product> findByProductIdAndDeletedAtIsNull(UUID productId);

	boolean existsByCompanyIdAndProductNameAndDeletedAtIsNull(UUID companyId, String productName);
	
	int countByCompanyIdAndDeletedAtIsNull(UUID companyId);
	
	@Modifying(clearAutomatically = true)
	@Query("""
			UPDATE Product p SET p.companyName = :companyName
			WHERE p.companyId = :companyId
					AND p.deletedAt IS NULL
	""")
	int updateCompanyNameByCompanyId(UUID companyId, String companyName);

	
	@Query("""
			SELECT p FROM Product p
			WHERE p.deletedAt IS NULL
					AND (:productName IS NULL OR :productName = '' OR p.productName LIKE CONCAT('%', :productName, '%'))
					AND (:isCompanyEmpty = true OR p.companyId IN :companyIdsQuery)
	""")
	Page<Product> search(List<UUID> companyIdsQuery, boolean isCompanyEmpty, String productName, Pageable pageable);
}
