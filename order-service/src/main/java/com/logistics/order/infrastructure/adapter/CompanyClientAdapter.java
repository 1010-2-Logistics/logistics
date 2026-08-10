package com.logistics.order.infrastructure.adapter;

import com.logistics.order.application.dto.result.CompanyOrderInfoResult;
import com.logistics.order.application.port.CompanyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyClientAdapter implements CompanyPort {

    @Override
    public CompanyOrderInfoResult getCompaniesForOrder(UUID startCompanyId, UUID endCompanyId) {
        return null;
    }
}

