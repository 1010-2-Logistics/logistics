package com.logistics.slack.application.port;

import com.logistics.slack.application.dto.result.UserInfo;

public interface UserPort {
    UserInfo getUser(Long userId);
}
