package com.logistics.ai.application.dto.internal;

import java.util.UUID;

public record HubInfo(
		UUID hubId,
		String hubName,
		String hubAddress
) {

}
