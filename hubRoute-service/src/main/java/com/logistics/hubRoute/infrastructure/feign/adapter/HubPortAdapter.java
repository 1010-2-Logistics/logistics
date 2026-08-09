package com.logistics.hubRoute.infrastructure.feign.adapter;

import com.logistics.hubRoute.application.port.HubPort;
import com.logistics.hubRoute.infrastructure.feign.client.HubClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HubPortAdapter implements HubPort {

    private final HubClient hubClient;


    @Override
    public Set<UUID> validateHubIds(List<UUID> hubIds) {
        return  hubClient.validateHubIds(hubIds);
    }
}
