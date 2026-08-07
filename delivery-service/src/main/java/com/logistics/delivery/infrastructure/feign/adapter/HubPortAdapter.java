package com.logistics.delivery.infrastructure.feign.adapter;

import com.logistics.delivery.application.port.HubPort;
import com.logistics.delivery.infrastructure.feign.client.HubClient;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubPortAdapter implements HubPort {

    private final HubClient hubClient;

    @Override
    public Set<UUID> validateHubIds(List<UUID> hubIds) {
        return hubClient.validateHubIds(hubIds);
    }
}