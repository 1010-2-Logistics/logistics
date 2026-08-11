package com.logistics.delivery.infrastructure.feign.adapter;

import com.logistics.delivery.application.port.HubRoutePort;
import com.logistics.delivery.infrastructure.feign.client.HubRouteClient;
import com.logistics.delivery.infrastructure.feign.response.HubRoutePathApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubRoutePortAdapter implements HubRoutePort {

    private final HubRouteClient hubRouteClient;

    @Override
    public List<HubRouteSegment> findRoute(UUID startHubId, UUID endHubId) {
        HubRoutePathApiResponse response = hubRouteClient.findHubRoute(startHubId, endHubId);
        return response.data().steps().stream()
                .map(step -> new HubRouteSegment(step.startHubId(), step.endHubId(), step.distance(), step.duration()))
                .toList();
    }
}