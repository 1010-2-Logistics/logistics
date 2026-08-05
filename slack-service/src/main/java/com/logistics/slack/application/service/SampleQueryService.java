package com.logistics.slack.application.service;

import com.logistics.slack.application.dto.query.GetSampleQuery;
import com.logistics.slack.application.dto.query.SearchSampleQuery;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackQueryRepository;
import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.global.exception.SlackErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SampleQueryService {

    private final SlackQueryRepository sampleQueryRepository;

    public Slack get(GetSampleQuery query) {
        return sampleQueryRepository.findByIdAndDeletedAtIsNull(query.sampleId())
                .orElseThrow(() -> new CustomException(SlackErrorCode.SAMPLE_NOT_FOUND));
    }

    public Page<Slack> search(SearchSampleQuery query) {
        return sampleQueryRepository.search(query.keyword(), query.pageable());
    }
}
