package com.logistics.order.infrastructure.adapter;

import com.logistics.order.application.dto.result.CompanyOrderInfoResult;
import com.logistics.order.application.port.CompanyPort;
import com.logistics.order.infrastructure.feign.client.CompanyClient;
import com.logistics.order.infrastructure.feign.response.CompanyOrderInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyClientAdapter implements CompanyPort {
    private final CompanyClient companyClient;

    @Override
    public CompanyOrderInfoResult getCompaniesForOrder(
            UUID startCompanyId,
            UUID endCompanyId
    ) {
        CompanyOrderInfoResponse companyOrderInfoResponse = companyClient.getCompaniesForOrder(
                startCompanyId,
                endCompanyId
        ).getData();

        return new CompanyOrderInfoResult(
                companyOrderInfoResponse.startCompanyId(),
                companyOrderInfoResponse.startHubId(),
                companyOrderInfoResponse.startCompanyAddress(),
                companyOrderInfoResponse.endCompanyId(),
                companyOrderInfoResponse.endHubId(),
                companyOrderInfoResponse.endCompanyAddress()
        );
    }
}

