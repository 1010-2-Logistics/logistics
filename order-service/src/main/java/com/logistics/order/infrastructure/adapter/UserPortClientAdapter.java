package com.logistics.order.infrastructure.adapter;


import com.logistics.order.application.dto.result.UserInfoResult;
import com.logistics.order.application.port.UserPort;
import com.logistics.order.infrastructure.feign.client.UserClient;
import com.logistics.order.infrastructure.feign.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPortClientAdapter implements UserPort {
    private final UserClient userClient;

    @Override
    public UserInfoResult getUser(Long userId) {
        UserInfoResponse response = userClient.getUser(userId).getData();

        return new UserInfoResult(
                response.userId(),
                response.name(),
                response.slackId()
        );
    }
}
