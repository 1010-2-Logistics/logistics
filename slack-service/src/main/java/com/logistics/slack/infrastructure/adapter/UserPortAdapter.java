package com.logistics.slack.infrastructure.adapter;


import com.logistics.slack.application.dto.result.UserInfo;
import com.logistics.slack.application.port.UserPort;
import com.logistics.slack.domain.entity.Role;
import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.global.exception.SlackErrorCode;
import com.logistics.slack.infrastructure.feign.client.UserClient;
import com.logistics.slack.infrastructure.feign.response.UserInfoResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPortAdapter implements UserPort {
    private final UserClient userClient;

    @Override
    public UserInfo getUser(
            Long userId
    ) {
        try {
            UserInfoResponse response = userClient.getUser(userId);

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
                    Role.valueOf(response.role()),
                    response.hubId(),
                    response.companyId()
            );

        } catch (FeignException.NotFound e) {
            throw new CustomException(
                    SlackErrorCode.SLACK_RECEIVER_NOT_FOUND
            );

        } catch (FeignException e) {
            throw new CustomException(
                    SlackErrorCode.SLACK_EXTERNAL_SERVICE_UNAVAILABLE
            );
        }
    }
}
