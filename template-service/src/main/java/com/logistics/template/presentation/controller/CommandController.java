package com.logistics.template.presentation.controller;

import com.logistics.template.application.dto.command.UpdateSampleCommand;
import com.logistics.template.application.facade.SampleFacade;
import com.logistics.template.application.service.SampleCommandService;
import com.logistics.template.global.response.ApiResponse;
import com.logistics.template.presentation.controller.dto.request.SampleCreateRequest;
import com.logistics.template.presentation.controller.dto.request.SampleUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/samples")
@RequiredArgsConstructor
public class CommandController {

    private final SampleFacade sampleFacade;
    private final SampleCommandService sampleCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UUID> create(@Valid @RequestBody SampleCreateRequest request) {
        UUID sampleId = sampleFacade.createSample(request.toCommand());
        return ApiResponse.success(201, "샘플 생성 성공", sampleId);
    }

    @PutMapping("/{sampleId}")
    public ApiResponse<Void> update(@PathVariable UUID sampleId, @Valid @RequestBody SampleUpdateRequest request) {
        sampleCommandService.update(new UpdateSampleCommand(sampleId, request.name()));
        return ApiResponse.success(200, "샘플 수정 성공", null);
    }

    @DeleteMapping("/{sampleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID sampleId) {
        // TODO: 인증 붙으면 실제 로그인 사용자로 교체
        sampleCommandService.delete(sampleId, "system");
    }
}
