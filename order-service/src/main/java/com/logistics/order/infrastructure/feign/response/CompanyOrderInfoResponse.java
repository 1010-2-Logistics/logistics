package com.logistics.order.infrastructure.feign.response;

import java.util.UUID;

public record CompanyOrderInfoResponse(
        UUID startCompanyId,
        UUID startHubId,
        String startCompanyAddress,
        UUID endCompanyId,
        UUID endHubId,
        String endCompanyAddress
) {
}
