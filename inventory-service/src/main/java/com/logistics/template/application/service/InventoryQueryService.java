package com.logistics.template.application.service;

import com.logistics.template.application.dto.query.GetInventoryQuery;
import com.logistics.template.application.dto.query.SearchInventoryQuery;
import com.logistics.template.domain.entity.Inventory;
import com.logistics.template.domain.repository.InventoryQueryRepository;
import com.logistics.template.global.exception.CustomException;
import com.logistics.template.global.exception.InventoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryQueryService {

    private final InventoryQueryRepository sampleQueryRepository;

    public Inventory get(GetInventoryQuery query) {
        return sampleQueryRepository.findByIdAndDeletedAtIsNull(query.sampleId())
                .orElseThrow(() -> new CustomException(InventoryErrorCode.SAMPLE_NOT_FOUND));
    }

    public Page<Inventory> search(SearchInventoryQuery query) {
        return sampleQueryRepository.search(query.keyword(), query.pageable());
    }
}
