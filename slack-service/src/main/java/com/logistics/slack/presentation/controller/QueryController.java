package com.logistics.slack.presentation.controller;

import com.logistics.slack.application.dto.query.GetSampleQuery;
import com.logistics.slack.application.dto.query.SearchSampleQuery;
import com.logistics.slack.application.service.SampleQueryService;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.global.response.ApiResponse;
import com.logistics.slack.global.response.PageResponse;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/samples")
@RequiredArgsConstructor
public class QueryController {

    private final SampleQueryService sampleQueryService;

    @GetMapping("/{sampleId}")
    public ApiResponse<SampleResponse> get(@PathVariable UUID sampleId) {
        Slack sample = sampleQueryService.get(new GetSampleQuery(sampleId));
        return ApiResponse.success(200, "샘플 조회 성공", SampleResponse.from(sample));
    }

    @GetMapping
    public ApiResponse<PageResponse<SampleSummaryResponse>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<Slack> page = sampleQueryService.search(new SearchSampleQuery(keyword, pageable));
        Page<SampleSummaryResponse> responsePage = page.map(SampleSummaryResponse::from);
        return ApiResponse.success(200, "샘플 목록 조회 성공", PageResponse.of(responsePage));
    }
}
