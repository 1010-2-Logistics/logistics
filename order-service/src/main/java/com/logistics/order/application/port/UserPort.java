package com.logistics.order.application.port;

import com.logistics.order.application.dto.result.UserInfoResult;

public interface UserPort {
    UserInfoResult getUser(
            Long userId
    );
}
