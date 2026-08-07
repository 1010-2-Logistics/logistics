package com.logistics.product.application.facade;

import org.springframework.stereotype.Component;

import com.logistics.product.application.service.ProductQueryService;
import com.logistics.product.infrastructure.feign.client.CompanyClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductQueryFacade {

	private final ProductQueryService producyQueryService;
	
	private final CompanyClient companyClient;
	
	
}
