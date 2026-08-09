package com.logistics.ai.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.port.out.DeliveryPort;
import com.logistics.ai.infrastructure.feign.client.DeliveryClient;
import com.logistics.ai.infrastructure.feign.client.DeliveryRouteClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryAdapter implements DeliveryPort {

	private final DeliveryClient deliveryClient;
	
	private final DeliveryRouteClient DeliveryRouteClient;
}
