package com.logistics.ai.application.port.out;

import com.logistics.ai.application.dto.internal.UserInfo;

public interface UserPort {

	UserInfo getUserInfo(Long userId);
}
