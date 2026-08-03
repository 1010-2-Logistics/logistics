package com.logistics.template.presentation.controller;

import com.logistics.template.application.dto.query.GetInventoryQuery;
import com.logistics.template.application.dto.query.SearchInventoryQuery;
import com.logistics.template.application.service.InventoryQueryService;
import com.logistics.template.domain.entity.Inventory;
import com.logistics.template.global.response.ApiResponse;
import com.logistics.template.global.response.PageResponse;
import com.logistics.template.presentation.controller.dto.response.SampleResponse;
import com.logistics.template.presentation.controller.dto.response.SampleSummaryResponse;
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

    private final InventoryQueryService sampleQueryService;

    @GetMapping("/{sampleId}")
    public ApiResponse<SampleResponse> get(@PathVariable UUID sampleId) {
        Inventory sample = sampleQueryService.get(new GetInventoryQuery(sampleId));
        return ApiResponse.success(200, "샘플 조회 성공", SampleResponse.from(sample));
    }

    @GetMapping
    public ApiResponse<PageResponse<SampleSummaryResponse>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<Inventory> page = sampleQueryService.search(new SearchInventoryQuery(keyword, pageable));
        Page<SampleSummaryResponse> responsePage = page.map(SampleSummaryResponse::from);
        return ApiResponse.success(200, "샘플 목록 조회 성공", PageResponse.of(responsePage));
    }
}
