package com.logistics.product.application.service;

import org.springframework.stereotype.Service;

import com.logistics.product.domain.repository.ProductQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

	private final ProductQueryRepository productQueryRepository;
	
	
}
