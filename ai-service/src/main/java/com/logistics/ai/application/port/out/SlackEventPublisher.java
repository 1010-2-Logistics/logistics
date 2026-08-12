package com.logistics.ai.application.port.out;

import com.logistics.ai.application.event.SlackEvent;

public interface SlackEventPublisher {
	void publish(SlackEvent event);
}
