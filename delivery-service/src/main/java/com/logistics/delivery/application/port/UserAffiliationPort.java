package com.logistics.delivery.application.port;

import java.util.UUID;

public interface UserAffiliationPort {

    void changeAffiliation(Long userId, String role, UUID hubId);
}
