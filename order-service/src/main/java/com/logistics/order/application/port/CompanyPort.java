package com.logistics.order.application.port;

import com.logistics.order.application.dto.result.CompanyOrderInfoResult;

import java.util.UUID;

public interface CompanyPort {
    CompanyOrderInfoResult getCompaniesForOrder(
            UUID startCompanyId,
            UUID endCompanyId
    );
}
