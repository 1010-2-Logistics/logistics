package com.logistics.slack.infrastructure.adapter;


import com.logistics.slack.application.dto.result.UserInfo;
import com.logistics.slack.application.port.UserPort;
import com.logistics.slack.domain.entity.Role;
import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.global.exception.SlackErrorCode;
import com.logistics.slack.global.response.ApiResponse;
import com.logistics.slack.infrastructure.feign.client.UserClient;
import com.logistics.slack.infrastructure.feign.response.UserInfoResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPortAdapter implements UserPort {
    private final UserClient userClient;

    @Override
    public UserInfo getUser(
            Long userId
    ) {
        try {
            ApiResponse<UserInfoResponse> apiResponse = userClient.getUser(userId);
            UserInfoResponse response = apiResponse.getData();

            if (response == null
                    || response.slackId() == null
                    || response.slackId().isBlank()) {
                throw new CustomException(
                        SlackErrorCode.SLACK_RECEIVER_NOT_FOUND
                );
            }

            return new UserInfo(
                    response.userId(),
                    response.slackId(),
                    null,
                    null,
                    null
            );

        } catch (FeignException.NotFound e) {
            log.error(
                    "UserClient 404 - status={}, response={}",
                    e.status(),
                    e.contentUTF8()
            );
            throw new CustomException(
                    SlackErrorCode.SLACK_RECEIVER_NOT_FOUND
            );

        } catch (FeignException e) {
            log.error(
                    "UserClient 호출 실패 - status={}, response={}",
                    e.status(),
                    e.contentUTF8()
            );
            throw new CustomException(
                    SlackErrorCode.SLACK_EXTERNAL_SERVICE_UNAVAILABLE
            );
        }
    }
}
