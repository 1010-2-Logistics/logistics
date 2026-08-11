package com.logistics.order.application.dto.result;

import java.util.UUID;

public record CompanyOrderInfoResult(
        UUID startCompanyId,
        UUID startHubId,
        String startCompanyAddress,
        UUID endCompanyId,
        UUID endHubId,
        String endCompanyAddress
) {
}
