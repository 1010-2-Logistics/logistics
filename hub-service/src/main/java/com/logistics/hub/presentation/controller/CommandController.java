package com.logistics.hub.presentation.controller;

import com.logistics.hub.application.dto.command.HubCreateCommand;
import com.logistics.hub.application.service.HubCommandService;
import com.logistics.hub.global.response.ApiResponse;
import com.logistics.hub.presentation.dto.dto.request.HubCreateRequestDto;
import com.logistics.hub.presentation.dto.dto.response.HubCreateResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class CommandController {


    private final HubCommandService hubCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HubCreateResponseDto> createHub(
            @Valid @RequestBody HubCreateRequestDto hubCreateRequestDto) {

        HubCreateCommand hubCreateCommand = HubCreateCommand.from(hubCreateRequestDto);
        HubCreateResponseDto hubCreateResponseDto = hubCommandService.createHub(hubCreateCommand);

        return ApiResponse.success(201, "허브 생성 성공", hubCreateResponseDto);
    }

//    @PutMapping("/{hubId}")
//    public ApiResponse<Void> update(@PathVariable UUID hubId, @Valid @RequestBody HubUpdateRequest request) {
//        hubCommandService.update(new UpdateHubCommand(hubId, request.name()));
//        return ApiResponse.success(200, "허브 수정 성공", null);
//    }

//    @DeleteMapping("/{hubId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void delete(@PathVariable UUID hubId) {
//        // TODO: 인증 붙으면 실제 로그인 사용자로 교체
//        hubCommandService.delete(hubId, "system");
//    }
}
