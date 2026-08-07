package com.logistics.company.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.logistics.product.application.facade.ProductFacade;
import com.logistics.product.application.port.CompanyPort;
import com.logistics.product.application.port.HubPort;
import com.logistics.product.application.service.ProductCommandService;
import com.logistics.product.application.service.ProductPolicy;
import com.logistics.product.application.service.ProductQueryService;

@ExtendWith(MockitoExtension.class)
public class ProductFacadeTest {

	@Mock
	private ProductCommandService productCommandService;
	
	@Mock
	private ProductQueryService productQueryService;
	
	@Mock
	private ProductPolicy policy;
	
	@Mock
	private CompanyPort companyPort;
	
	@Mock
	private HubPort hubPort;
	
	@InjectMocks
	private ProductFacade productFacade;
	
	@Nested
	@DisplayName("상품 등록 테스트")
	class CreateProduct {
		/*
		 * 상품 등록 테스트 목록
		 * 
		 */
	}
	
	@Nested
	@DisplayName("상품 수정 테스트")
	class UpdateProduct {
		/*
		 * 상품 수정 테스트 목록
		 * 
		 */
	}
	
	@Nested
	@DisplayName("상품 삭제 테스트")
	class DeleteProduct {
		/*
		 * 상품 삭제 테스트 목록
		 * 
		 */
	}
	
}
