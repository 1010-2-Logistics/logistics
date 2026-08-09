package com.logistics.delivery.infrastructure.feign.request;

import java.util.UUID;

public record HubRouteFindRequest(UUID startHubId, UUID endHubId) {
}