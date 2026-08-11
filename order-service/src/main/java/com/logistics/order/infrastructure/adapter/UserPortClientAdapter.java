package com.logistics.order.infrastructure.adapter;


import com.logistics.order.application.dto.result.UserInfoResult;
import com.logistics.order.application.port.UserPort;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import com.logistics.order.infrastructure.feign.client.UserClient;
import com.logistics.order.infrastructure.feign.response.UserInfoResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPortClientAdapter implements UserPort {
    private final UserClient userClient;

    @Override
    public UserInfoResult getUser(Long userId) {
        try {
            UserInfoResponse response = userClient.getUser(userId).getData();

            return new UserInfoResult(
                    response.userId(),
                    response.name(),
                    response.slackId()
            );
        } catch (FeignException e) {
            log.error(
                    "User service 호출 실패. status={}, response={}",
                    e.status(),
                    e.contentUTF8(),
                    e
            );
            throw new CustomException(
                    OrderErrorCode.ORDER_SERVICE_UNAVAILABLE
            );
        }
    }
}
