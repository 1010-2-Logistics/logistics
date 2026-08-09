package com.logistics.ai.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.port.out.HubPort;
import com.logistics.ai.infrastructure.feign.client.HubClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HubAdapter implements HubPort {

	private final HubClient hubClient;
}
