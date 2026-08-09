package com.logistics.ai.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.port.out.OrderPort;
import com.logistics.ai.infrastructure.feign.client.OrderClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderAdapter implements OrderPort {

	private final OrderClient orderClient;
}
