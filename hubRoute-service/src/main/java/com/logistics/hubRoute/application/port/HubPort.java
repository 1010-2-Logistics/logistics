package com.logistics.hubRoute.application.port;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface HubPort {
    Set<UUID> validateHubIds(List<UUID> hubIds);
}
