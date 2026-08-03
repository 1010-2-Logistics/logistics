package com.logistics.template.application.service;

import com.logistics.template.application.dto.query.GetSampleQuery;
import com.logistics.template.application.dto.query.SearchSampleQuery;
import com.logistics.template.domain.entity.Sample;
import com.logistics.template.domain.repository.SampleQueryRepository;
import com.logistics.template.global.exception.CustomException;
import com.logistics.template.global.exception.SampleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SampleQueryService {

    private final SampleQueryRepository sampleQueryRepository;

    public Sample get(GetSampleQuery query) {
        return sampleQueryRepository.findByIdAndDeletedAtIsNull(query.sampleId())
                .orElseThrow(() -> new CustomException(SampleErrorCode.SAMPLE_NOT_FOUND));
    }

    public Page<Sample> search(SearchSampleQuery query) {
        return sampleQueryRepository.search(query.keyword(), query.pageable());
    }
}
