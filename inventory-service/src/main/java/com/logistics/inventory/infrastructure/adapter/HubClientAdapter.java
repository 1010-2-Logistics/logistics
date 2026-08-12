package com.logistics.inventory.infrastructure.adapter;

import com.logistics.inventory.application.dto.result.HubExistsResponseDto;
import com.logistics.inventory.application.port.HubPort;
import com.logistics.inventory.global.exception.CommonErrorCode;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.infrastructure.feign.client.HubClient;
import com.logistics.inventory.infrastructure.feign.response.HubValidationResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HubClientAdapter implements HubPort {
    private final HubClient hubClient;

    @Override
    public HubExistsResponseDto getHub(UUID hubId) {
        try {
            Set<UUID> validIds = hubClient.validateHubIds(List.of(hubId));
            return new HubExistsResponseDto(hubId, validIds.contains(hubId));
        } catch (FeignException e) {
            throw new CustomException(CommonErrorCode.INVENTORY_SERVICE_UNAVAILABLE);
        }
    }
}
