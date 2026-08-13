package com.logistics.ai.application.event;

import java.util.UUID;

public record SlackEvent(
		Long receiverId,
		String message,
		UUID referenceId
) {

}
