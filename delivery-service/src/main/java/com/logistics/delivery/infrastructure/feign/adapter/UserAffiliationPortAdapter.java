package com.logistics.delivery.infrastructure.feign.adapter;

import com.logistics.delivery.application.port.UserAffiliationPort;
import com.logistics.delivery.infrastructure.feign.client.UserAffiliationClient;
import com.logistics.delivery.infrastructure.feign.request.UserAffiliationRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAffiliationPortAdapter implements UserAffiliationPort {

    private final UserAffiliationClient userAffiliationClient;

    @Override
    public void changeAffiliation(Long userId, String role, UUID hubId) {
        userAffiliationClient.changeAffiliation(userId, new UserAffiliationRequest(role, null, hubId));
    }
}
