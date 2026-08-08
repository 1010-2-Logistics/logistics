package com.logistics.product.domain.entity;

import java.util.UUID;

import com.logistics.product.global.entity.BaseEntity;
import com.logistics.product.global.exception.ProductErrorCode;
import com.logistics.product.global.exception.ProductException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID productId;
	
	@Column(name = "company_id", nullable = false)
	private UUID companyId;
	
	@Column(name = "product_name", nullable = false, length = 20)
	private String productName;
	
	@Column(name = "company_name", nullable = false)
	private String companyName;
	
	public static Product create(UUID companyId, String productName, String companyName) {
		Product product = new Product();
		
		product.companyId = companyId;
		product.productName = productName;
		product.companyName = companyName;
		
		return product;
	}
	
	public void updateProductName(String productName) {
		if(isDeleted()) {
			throw new ProductException(ProductErrorCode.PRODUCT_DELETED_CONFLICT);
		}
		
		this.productName = productName;
	}
	
	public void updateCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	/**
	 * ture: 삭제된 상품
	 * @return
	 */
	public boolean isDeleted() {
		if(this.getDeletedAt() != null) {
			return true;
		}
		
		return false;
	}
	
}
