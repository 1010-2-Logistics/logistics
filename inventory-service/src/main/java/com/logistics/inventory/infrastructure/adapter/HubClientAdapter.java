package com.logistics.inventory.infrastructure.adapter;

import com.logistics.inventory.application.dto.result.HubExistsResponseDto;
import com.logistics.inventory.application.port.HubPort;
import com.logistics.inventory.infrastructure.feign.client.HubClient;
import com.logistics.inventory.infrastructure.feign.response.HubValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HubClientAdapter implements HubPort {
    private final HubClient hubClient;

    @Override
    public HubExistsResponseDto getHub(UUID hubId) {
        HubValidationResponse hubValidationResponse = hubClient.getHub(hubId).getData();

        return new HubExistsResponseDto(
                hubValidationResponse.hubId(),
                hubValidationResponse.exists()
        );
    }
}
