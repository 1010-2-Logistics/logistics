package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.query.SearchDeliveryManagerQuery;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryManagerQueryService {

    private final DeliveryManagerRepository deliveryManagerRepository;

    public DeliveryManager getById(Long deliveryManagerId) {
        return deliveryManagerRepository.findByIdAndDeletedAtIsNull(deliveryManagerId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND));
    }

    public Page<DeliveryManager> search(SearchDeliveryManagerQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return deliveryManagerRepository.search(query.managerType(), query.hubId(), pageRequest);
    }
}