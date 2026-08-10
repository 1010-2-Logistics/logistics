package com.logistics.order.infrastructure.adapter;

import com.logistics.order.application.dto.result.CompanyOrderInfoResult;
import com.logistics.order.application.port.CompanyPort;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import com.logistics.order.infrastructure.feign.client.CompanyClient;
import com.logistics.order.infrastructure.feign.response.CompanyOrderInfoResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyClientAdapter implements CompanyPort {
    private final CompanyClient companyClient;

    @Override
    public CompanyOrderInfoResult getCompaniesForOrder(
            UUID startCompanyId,
            UUID endCompanyId
    ) {
        try {
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
        } catch (FeignException.NotFound e) {
            throw new CustomException(
                    OrderErrorCode.ORDER_REFERENCE_NOT_FOUND
            );
        } catch (FeignException e) {
            log.error(
                    "Company service 호출 실패. status={}, response={}",
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
