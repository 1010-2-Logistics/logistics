package com.logistics.ai.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.logistics.ai.application.port.in.DispatchDeadlineUseCase;
import com.logistics.ai.application.port.out.DeliveryPort;
import com.logistics.ai.application.port.out.DeliveryRoutPort;
import com.logistics.ai.application.port.out.OrderPort;
import com.logistics.ai.application.port.out.ProductPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DispatchDeadlineService implements DispatchDeadlineUseCase {
	
	private final OrderPort orderPort;
	
	private final DeliveryPort deliveryPort;
	
	private final DeliveryRoutPort deliveryRoutePort;
	
	private final ProductPort productPort;
	
	@Override
	public void generate(UUID orderId, UUID deliveryId) {
		
	}
	
}
