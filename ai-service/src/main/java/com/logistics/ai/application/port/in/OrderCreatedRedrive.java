package com.logistics.ai.application.port.in;

import com.logistics.ai.application.dto.result.DlqRedriveResult;

public interface OrderCreatedRedrive {
	DlqRedriveResult redrive(int count);
}
