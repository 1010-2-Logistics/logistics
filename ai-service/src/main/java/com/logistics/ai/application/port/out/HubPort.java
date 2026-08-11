package com.logistics.ai.application.port.out;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.logistics.ai.application.dto.internal.HubInfo;

public interface HubPort {

	List<HubInfo> getHubInfo(Set<UUID> hubIds);
}
