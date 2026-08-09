package com.logistics.ai.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.port.out.ProductPort;
import com.logistics.ai.infrastructure.feign.client.ProductClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductPort {

	private final ProductClient productClient;
}
